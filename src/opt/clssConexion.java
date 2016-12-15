/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package opt;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
/**
 *
 * @author root
 */
public class clssConexion {
    public Connection cnn;
    public ResultSet rs;
    public Statement stmt;
    public static String servidor="",db="",usuario="",clave="";
    public static String mensaje="",reporte="";
    static int pky = 167;
    public javax.swing.table.DefaultTableModel model = null;
    public int countfilas = 0,count=0,pag=0,progress=0;
    public ResultSetMetaData metaDatos = null;
    public Object[] fila = null;
    
    public boolean cnnconexiongp(String ip){
        boolean stcnn = false;
        Properties prop = new Properties();
        FileInputStream str = null;
        try{
            //Inicializando las variables para la conexiÃ³n
            str = new FileInputStream("configmail.properties");
            prop.load(str);
            str.close();
            //servidor = prop.getProperty("SERVER");
            servidor = ip;
            db = prop.getProperty("DBPOSTGRES");
            usuario = prop.getProperty("USER");
            clave = prop.getProperty("CLAVEDB");
            mensaje = prop.getProperty("MENSAJE");
            //reporte = prop.getProperty("REPORTES");
        }catch(IOException e){
            System.out.println(e.toString());
            return false;
        }
        String strCnn = "jdbc:postgresql://"+servidor+"/"+db;
        //System.out.println(strCnn);
        try{
            //Accesando al Driver de ConexiÃ³n a POSTGRES
            Class.forName("org.postgresql.Driver");
            //System.out.println("usuario: "+usuario);
            //System.out.println("clave: "+clave);
            cnn = DriverManager.getConnection(strCnn,usuario,clave);
            stcnn = true;
        }catch(ClassNotFoundException | SQLException e){
            System.out.println(e.toString());
            stcnn = false;
        }
        return stcnn;
    }
    public boolean cnnconexiongp(String Host,String Db,String Port,String Usuario,String Clave){
        boolean stcnn = false;
        servidor = Host;
        db = Db;
        usuario = Usuario;
        clave = Clave;
        mensaje = "";
        String strCnn = "jdbc:postgresql://"+servidor+"/"+db;
        //System.out.println(strCnn);
        try{
            //Accesando al Driver de ConexiÃ³n a POSTGRES
            Class.forName("org.postgresql.Driver");
            //System.out.println("usuario: "+usuario);
            //System.out.println("clave: "+clave);
            cnn = DriverManager.getConnection(strCnn,usuario,clave);
            stcnn = true;
        }catch(ClassNotFoundException | SQLException e){
            System.out.println(e.toString());
            stcnn = false;
        }
        return stcnn;
    }
    
    public boolean crearStatement(){
        try{
           stmt = cnn.createStatement();
           return true;
        }catch(Exception e){
            System.out.println(e.toString());
            return false;
        }
    }
    public boolean actualizarStatement(){
        try{
            stmt = cnn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            return true;
        }catch(Exception e){
            System.out.println(e.toString());
            return false;
        }
    }
    public boolean execSQL(String SQL){
        try{
            rs = stmt.executeQuery(SQL);
            return true;
        }catch(Exception e){
           System.out.println(e.toString());
           return false;
        }
    }
    public int execSQLIUD(String SQL){
        int afectados = 0;
        try{
            afectados = stmt.executeUpdate(SQL);
        }catch(Exception e){
            System.out.println(e.toString());
        }
        return afectados;
    }
    public void cerrarConexion(){
        try{
            if(!cnn.isClosed() || !stmt.isClosed() || !rs.isClosed()){
               stmt.execute("END");
               rs.close();
               cnn.close();
            }
        }catch(Exception e){
            System.out.println(e.toString());
        }
    }
    public String encrypta(String valor) {
        String resultado = "";
        int numero = 0;
        String[] textoLetras = new String[valor.length()];
        int a = 0;
        for (int i = 0; i < valor.length(); i++) {
            textoLetras[i] = String.valueOf((int) valor.charAt(i));
        }
        for (int b = 0; b < valor.length(); b++) {
            if ((a + 1) >= valor.length() + 1) {
                a = 1;
            } else {
                a++;
            }
            numero = Integer.parseInt(textoLetras[b]) + pky;
            if (numero > 255) {
                numero = numero - 255;
            }
            if (b < valor.length() - 1) {
                resultado = resultado + String.valueOf(numero).trim() + ":";
            } else {
                resultado = resultado + String.valueOf(numero).trim();
            }
        }
        return resultado;
    }
    /**
     * Función para desencryptar los datos
     * @param valor - Valor que deseamos desencryptar
     * @return
     */
    public String desencrypta(String valor) {
        String resultado = "";
        int numero = 0;
        String[] textoLetras = valor.split(":");
        for (int i = 0; i < textoLetras.length; i++) {
            numero = Integer.parseInt(textoLetras[i]) - pky;
            if (numero < 0) {
                numero = numero + 255;
            }
            resultado = resultado + String.valueOf((char) (numero));
        }
        resultado.trim();

        return resultado;
    }
    public String readArchivo(String ruta,String file){
        String read = "";
        File arch = new File(ruta+file);
        FileReader lector = null;
        BufferedReader tinta =  null;
        try{
            lector = new FileReader(arch);
            tinta = new BufferedReader(lector);
            String linea = "";
            while((linea = tinta.readLine()) != null){
                read += linea+"\n";
            }
            lector.close();
            tinta.close();
        }catch(Exception e){
            System.out.println(e.toString());
            //this.msgbox(e.toString());
        }
        return read;
    }
}
