/*
 * Licencia:
 * Sonda Panamá / PTY 2016
 * Version 1.0
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package opt;

/**
 * Objetivo:
 * La funcionalidad de esta aplicación es la de enviar correos, haciendo la función de enviar reportes y 
 * enviar alertas de advertencias a los integrantes de la lista de grupo.
 * 
 * @author jorge.cisneros
 * email: jorge.cisneros@sonda.com / jcisneros.cisneros250@gmail.com
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class clssMssql
{
    Connection conn = null;
    Statement stmt = null;
    ResultSet rs = null;
    int stacker = 0;
    String posid = "";
    java.util.Date dia = new java.util.Date();
    String db_connect_string = "";
    String db_driver = "";
    String db_userid = "";
    String db_password = "";
    
    /**
     * Función que inicializa las variables de conexión.
     * @param db_connect_string - Url de la conexión en dirección ip, puertos, base de datos
     * @param db_driver - Driver de conexión
     * @param db_userid - usuario de conexión
     * @param db_password - clave de conexión
     */
   @SuppressWarnings("CallToPrintStackTrace")
   public void dbConnect(String db_connect_string, String db_driver, String db_userid, String db_password) 
   {
      this.db_connect_string = db_connect_string;
      this.db_driver = db_driver;
      this.db_userid = db_userid;
      this.db_password = db_password;
   }
   
   /**
    * Función que abre la conexión a la base de datos.
    * @return si es true o false.
    */
   public boolean isConnect(){
       try {
         Class.forName(db_driver).newInstance();
         conn = DriverManager.getConnection(db_connect_string, db_userid, db_password);
         return true;
      } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | SQLException e ) {
         return false;
      }
   }
   
   /**
    * 
    * @return 
    */
   public boolean crearStatement(){
        try{
           stmt = conn.createStatement();
           return true;
        }catch(SQLException e){
            System.out.println(e.toString());
            return false;
        }
    }
   
   public boolean execSQL(String SQL){
        try{
            rs = stmt.executeQuery(SQL);
            return true;
        }catch(SQLException e){
           System.out.println("Error en RS: "+e.toString());
           return false;
        }
    }
   
   public void cerrarConexion(){
        try{
            if(!conn.isClosed() || !stmt.isClosed() || !rs.isClosed()){
               //stmt.execute("END");
               stmt.close();
               rs.close();
               conn.close();
            }
        }catch(SQLException e){
            System.out.println(e.toString());
        }
    }
   
   public void cerrar(){
        try {
            rs.close();
            conn.close();
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(clssMssql.class.getName()).log(Level.SEVERE, null, ex);
        }
   }
   public int getCantStacker(){
       return 5;//this.stacker;
   }
   public String getPosId(){
       return this.posid;
   }
   
    @SuppressWarnings("UnusedAssignment")
   public String muestraData(ResultSet r) throws Exception
{
    String txtSalida = "";
    ResultSetMetaData rmeta= r.getMetaData();
    int numColumnas= rmeta.getColumnCount();
    txtSalida="<br/>";
    txtSalida="<center>";
    txtSalida+="<table border=1>";
    
    txtSalida+="<tr>";
    for(int i=1; i<=numColumnas; ++i)
        txtSalida += "<td><b>"+rmeta.getColumnName(i)+"</b></td>";
    txtSalida+="</tr>";

    while(r.next())
    {
        txtSalida+="<tr>";
        for(int i=1;i<=numColumnas;++i)
        {
            txtSalida += "<td>"+r.getString(i)+"</td>";
        }
        txtSalida+="</tr>";
    }
    txtSalida+="</table>";
    txtSalida+="</center>";
    return txtSalida;
}
   
   public String qryMonitorTaSondaMail(){
       String txtmsg="",qry = "SELECT  " +
            "a.terminal TERMINAL" +
            ",CONVERT(INT, CONVERT(VARBINARY, d.posid, 2)) POSID" +
            ",d.amid AMID " +
            ",g.referencia UBICACION " +
            ",cast(cast((cast(coalesce(O.cantbilletes,0) as NUMERIC(18,2)) / a.capacidadStaker * 100) as NUMERIC(18,0)) as varchar) + '% Stacker - OK' STACKER " +
            "From tasSonda.terminales d " +
            "	LEFT JOIN tasSonda.logStatus a on (d.descripcion=a.terminal) " +
            "	LEFT JOIN tasSonda.V_STATUS_TAS_TOTAL T ON (a.stamp=T.fecha) " +
            "	LEFT JOIN tasSonda.codigos b ON a.impresora=b.codigo " +
            "	LEFT JOIN tasSonda.semaforo c ON b.idsemaforo=c.id " +
            "	LEFT JOIN (select max(fechaDocumento) fechaDocumento,terminal" +
            "		,(sum(cantbilletes_aprob) + sum(cantbilletes_reprob)) cantbilletes" +
            "		,(sum(montototal_aprob) + sum(montototal_reprob)) montototal" +
            "		,sum(cantbilletes_aprob) cantbilletes_aprob" +
            "		,sum(montototal_aprob) montototal_aprob" +
            "		,sum(cantbilletes_reprob) cantbilletes_reprob" +
            "		,sum(montototal_reprob) montototal_reprob" +
            "		,max(fechatrx) fechatrx " +
            "		,amid " +
            "		from " +
            "		(" +
            "			select terminal,(select amid from tasSonda.transactionLog_OffLine where stamp=a.fechatrx) amid" +
            "			,fechaDocumento,fechatrx,cantbilletes_aprob,montototal_aprob,cantbilletes_reprob,montototal_reprob from (" +
            "				select terminal,max(fechaDocumento) fechaDocumento,max(fechatrx) fechatrx,sum(cantbilletes_aprob) cantbilletes_aprob,sum(montototal_aprob) montototal_aprob" +
            "				,sum(cantbilletes_reprob) cantbilletes_reprob,sum(montototal_reprob) montototal_reprob " +
            "				from (" +
            "					select max(fechaDocumento) fechaDocumento,terminal,amid,count(*) cantbilletes_aprob,sum(montoTotal) montototal_aprob" +
            "					,0 cantbilletes_reprob,0 montototal_reprob,max(stamp) fechatrx" +
            "							from tasSonda.transactionLog_OffLine where corte=0 and codRespuesta='00' group by terminal,amid,codRespuesta " +
            "					union " +
            "					select max(fechaDocumento) fechaDocumento,terminal,amid,0 cantbilletes_aprob,0 montototal_aprob,count(*) cantbilletes_reprob" +
            "					,sum(montoTotal) montototal_reprob,max(stamp) fechatrx" +
            "							from tasSonda.transactionLog_OffLine where corte=0 and codRespuesta='09' group by terminal,amid,codRespuesta " +
            "				) x " +
            "				group by terminal " +
            "			) a " +
            "		) R " +
            "		group by terminal,amid) O " +
            "	ON (a.terminal=O.terminal) " +
            "	 LEFT JOIN tasSonda.agencias g ON d.operadorId=g.operadorId " +
            "where cast(stamp as date)=cast(getdate() as date) " +
            "and stamp=(select max(stamp) from tasSonda.logStatus)";
       if(isConnect()){
           if(crearStatement()){
                if(this.execSQL(qry)){
                    try {
                        txtmsg = this.muestraData(this.rs);
                    } catch (Exception ex) {
                        Logger.getLogger(clssMssql.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        this.cerrarConexion();
                    }
                }
            }
       }            
       return txtmsg;
   }
   /**
    * Función que genera el reporte de Monitor Tas Sonda enviado a Tesorería
    * @return txtmsg: salida en html de la consulta en sql.
    */
   public String qryMonitorTaSonda(){
       String txtmsg = "";
       String queryString = "select a.terminal,CONVERT(INT, CONVERT(VARBINARY, c.posid, 2)) posid,a.stamp,a.capacidadStaker,a.cantidadBilletes from " +
        "tasSonda.logStatus a " +
        ",(select terminal,max(stamp) stamp from tasSonda.logStatus where cast(stamp as date)=cast(getdate() as date) group by terminal) b " +
        ",tasSonda.terminales c " +
        "where a.terminal=b.terminal and a.stamp=b.stamp " +
        "and a.terminal=c.descripcion";
       
       if(isConnect()){
           if(crearStatement()){
                if(this.execSQL(queryString)){
                    try {
                        txtmsg = this.muestraData(this.rs);
                    } catch (Exception ex) {
                        Logger.getLogger(clssMssql.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        this.cerrarConexion();
                    }
                }
            }
       }
       return txtmsg;
   }
}