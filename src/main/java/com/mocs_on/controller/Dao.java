import java.sql.*;
import com.zaxxer.hikari.*;
public abstract class Dao{
    public Connection getConnection(){
        HdataSource ds=new HDataSource();
        ds.init();
        return ds.getConnection();
    }
}