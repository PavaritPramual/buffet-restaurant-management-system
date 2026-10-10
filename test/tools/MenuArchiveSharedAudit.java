import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import java.util.*;

/** Read-only catalog/Flyway metadata. No migrate, repair, data writes or credentials in output. */
public class MenuArchiveSharedAudit {
    public static void main(String[] args) throws Exception {
        if(args.length!=1)throw new IllegalArgumentException("Usage: MenuArchiveSharedAudit <repository-root>");
        Path root=Path.of(args[0]).toAbsolutePath();
        Map<String,String> env=new HashMap<>();
        for(String line:Files.readAllLines(root.resolve("code/backend/.env"),StandardCharsets.UTF_8)) {
            if(line.isBlank()||line.stripLeading().startsWith("#")||!line.contains("="))continue;
            int split=line.indexOf('='); String value=line.substring(split+1).trim();
            if(value.length()>1&&((value.startsWith("\"")&&value.endsWith("\""))||(value.startsWith("'")&&value.endsWith("'"))))value=value.substring(1,value.length()-1);
            env.put(line.substring(0,split).trim(),value);
        }
        String host=env.getOrDefault("SUPABASE_DB_HOST","");
        if(!host.endsWith(".pooler.supabase.com")||!env.getOrDefault("SUPABASE_DB_PORT","5432").equals("5432"))throw new IllegalArgumentException("Expected configured session pooler");
        String url="jdbc:postgresql://"+host+":5432/"+env.getOrDefault("SUPABASE_DB_NAME","postgres")+"?sslmode=require&connectTimeout=10&socketTimeout=20";
        try(var connection=DriverManager.getConnection(url,env.get("SUPABASE_DB_USERNAME"),env.get("SUPABASE_DB_PASSWORD"))) {
            connection.setReadOnly(true); connection.setAutoCommit(false);
            String query="""
                SELECT jsonb_build_object(
                  'observedAt',CURRENT_TIMESTAMP,'readOnly',current_setting('transaction_read_only'),
                  'history',(SELECT jsonb_agg(jsonb_build_object('version',version,'success',success) ORDER BY installed_rank) FROM public.flyway_schema_history),
                  'foreignKeys',(SELECT jsonb_agg(jsonb_build_object('name',c.conname,'child',c.conrelid::regclass::text,'parent',c.confrelid::regclass::text,'definition',pg_get_constraintdef(c.oid)) ORDER BY c.conname) FROM pg_constraint c WHERE c.contype='f' AND c.confrelid IN ('public.menu_items'::regclass,'public.menu_categories'::regclass)),
                  'archiveColumns',(SELECT jsonb_agg(jsonb_build_object('table',table_name,'type',data_type,'nullable',is_nullable)) FROM information_schema.columns WHERE table_schema='public' AND table_name IN ('menu_items','menu_categories') AND column_name='archived_at'),
                  'rls',(SELECT jsonb_agg(jsonb_build_object('table',relname,'enabled',relrowsecurity)) FROM pg_class WHERE oid IN ('public.menu_items'::regclass,'public.menu_categories'::regclass)),
                  'backendUpdate',(SELECT jsonb_agg(jsonb_build_object('table',relname,'allowed',has_table_privilege(current_user,oid,'UPDATE')) ORDER BY relname) FROM pg_class WHERE oid IN ('public.menu_items'::regclass,'public.menu_categories'::regclass)),
                  'clientGrants',(SELECT count(*) FROM pg_class c CROSS JOIN LATERAL aclexplode(COALESCE(c.relacl,acldefault('r',c.relowner))) a LEFT JOIN pg_roles r ON r.oid=a.grantee WHERE c.oid IN ('public.menu_items'::regclass,'public.menu_categories'::regclass) AND (a.grantee=0 OR r.rolname IN ('anon','authenticated')))
                )
                """;
            try(var statement=connection.createStatement(); var result=statement.executeQuery(query)){result.next();System.out.println(result.getString(1));}
            connection.rollback();
        }
    }
}
