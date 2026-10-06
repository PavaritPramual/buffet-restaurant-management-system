import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;
import org.flywaydb.core.Flyway;

/** Read-only Flyway validation and JDBC metadata. Never migrate, repair, or print credentials. */
public class SharedDatabaseReadiness {
    public static void main(String[] args) throws Exception {
        Path root = Path.of(args[0]).toAbsolutePath();
        Map<String, String> env = new HashMap<>();
        for (String line : Files.readAllLines(root.resolve("code/backend/.env"), StandardCharsets.UTF_8)) {
            if (line.isBlank() || line.stripLeading().startsWith("#") || !line.contains("=")) continue;
            int split = line.indexOf('=');
            String value = line.substring(split + 1).trim();
            if (value.length() > 1 && ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'")))) value = value.substring(1, value.length() - 1);
            env.put(line.substring(0, split).trim(), value);
        }
        String host = env.get("SUPABASE_DB_HOST");
        if (host == null || !host.endsWith(".pooler.supabase.com")) throw new IllegalArgumentException("Expected configured Supabase Session Pooler");
        if (!"5432".equals(env.getOrDefault("SUPABASE_DB_PORT", "5432"))) throw new IllegalArgumentException("Expected session pooling port 5432");
        String url = "jdbc:postgresql://" + host + ":5432/" + env.getOrDefault("SUPABASE_DB_NAME", "postgres") + "?sslmode=require";
        String username = env.get("SUPABASE_DB_USERNAME"), password = env.get("SUPABASE_DB_PASSWORD");
        Flyway flyway = Flyway.configure().dataSource(url, username, password).defaultSchema("public")
                .locations("filesystem:" + root.resolve("code/backend/src/main/resources/db/migration/common"),
                        "filesystem:" + root.resolve("code/backend/src/main/resources/db/migration/postgresql"))
                .outOfOrder(false).load();
        var validation = flyway.validateWithResult();
        int pending = flyway.info().pending().length;
        System.out.println("FLYWAY_VALIDATE success=" + validation.validationSuccessful + " outOfOrder=false pending=" + pending);
        try (var connection = DriverManager.getConnection(url, username, password)) {
            connection.setReadOnly(true);
            String sql = """
                SELECT jsonb_build_object(
                  'currentUser',current_user,'sessionUser',session_user,
                  'bypassRls',(SELECT rolbypassrls FROM pg_roles WHERE rolname=current_user),
                  'paymentSelect',has_table_privilege(current_user,'public.payments','SELECT'),
                  'paymentInsert',has_table_privilege(current_user,'public.payments','INSERT'),
                  'paymentSequenceUsage',has_sequence_privilege(current_user,pg_get_serial_sequence('public.payments','id'),'USAGE'),
                  'historySelect',has_table_privilege(current_user,'public.flyway_schema_history','SELECT'),
                  'anonPaymentSelect',has_table_privilege('anon','public.payments','SELECT'),
                  'authenticatedPaymentSelect',has_table_privilege('authenticated','public.payments','SELECT'),
                  'anonHistorySelect',has_table_privilege('anon','public.flyway_schema_history','SELECT'),
                  'authenticatedHistorySelect',has_table_privilege('authenticated','public.flyway_schema_history','SELECT'),
                  'rlsEnabled',(SELECT relrowsecurity FROM pg_class WHERE oid='public.payments'::regclass),
                  'clientTableGrants',(SELECT count(*) FROM information_schema.role_table_grants WHERE table_schema='public' AND grantee IN ('PUBLIC','anon','authenticated')),
                  'clientTableGrantDetails',(SELECT jsonb_agg(jsonb_build_object('table',table_name,'role',grantee,'privilege',privilege_type) ORDER BY table_name,grantee,privilege_type) FROM information_schema.role_table_grants WHERE table_schema='public' AND grantee IN ('PUBLIC','anon','authenticated')),
                  'clientSequenceAccess',(SELECT count(*) FROM pg_class c JOIN pg_namespace n ON n.oid=c.relnamespace CROSS JOIN (VALUES ('anon'),('authenticated')) r(role_name) WHERE n.nspname='public' AND c.relkind='S' AND (has_sequence_privilege(r.role_name,c.oid,'USAGE') OR has_sequence_privilege(r.role_name,c.oid,'SELECT') OR has_sequence_privilege(r.role_name,c.oid,'UPDATE'))),
                  'history',(SELECT jsonb_agg(jsonb_build_object('rank',installed_rank,'version',version,'success',success) ORDER BY installed_rank) FROM public.flyway_schema_history)
                )
                """;
            try (var statement = connection.createStatement(); var result = statement.executeQuery(sql)) {
                result.next();
                System.out.println("JDBC_METADATA " + result.getString(1));
            }
        }
        if (pending != 0) throw new IllegalStateException("Shared runtime has pending migrations: " + pending + "; review and deployment required, do not run migrate from this tool");
        if (!validation.validationSuccessful) throw new IllegalStateException("Flyway validation failed; inspect applied checksums before deployment");
    }
}
