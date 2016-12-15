/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package opt;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;
/**
 *
 * @author Ricardo Haynes
 */

public class clssIreports {
    clssConexion con = new clssConexion();
    private JasperReport jasperReport = null;
    private JasperPrint JPrint = null;
    public Map parameter = new HashMap();

    public void generarreporte(Map parametro,String reporte){
        JasperReport rep = null;
        JasperPrint prt = null;
        JasperViewer jv = null;
        try{            
            if(!reporte.isEmpty() || !reporte.equals("")){
                //System.out.println("Entreé");
                if(con.cnnconexiongp("")){//System.out.println("Conexion exitosa");
                        if(!parametro.isEmpty()){
                            //Generando con .jasper
                            this.jasperReport = JasperCompileManager.compileReport(reporte);
                            System.out.println("******COMPILADO**********");
                            JPrint = JasperFillManager.fillReport(jasperReport, parametro, con.cnn);
                            JasperExportManager.exportReportToPdfFile(JPrint, reporte.replace("jrxml", "pdf"));
                            //java.util.Locale localidad = new java.util.Locale("es","Spanish");
                            //JPrint.setLocaleCode(localidad.getISO3Language());
                            //jv = new JasperViewer(JPrint, false);
                            //jv.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
                            //jv.show();   
                        }else{
                            System.out.println("Parametros vacios");
                        }  
                }  
                try {
                    con.cnn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(clssIreports.class.getName()).log(Level.SEVERE, null, ex);
                }
            }else System.out.println("Debe ingresar el query, correctamente");
        }catch(JRException e){
            System.out.println("Error de Reporte "+reporte+": "+e.toString());
        }
    }
    
    public void generarreporte(Map parametro, String reporte, String nombrepdf, boolean vista, java.sql.Connection cnn) {
        try {
            if (!reporte.isEmpty() || !reporte.equals("")) {
                //if (pg.cnnPostGres()) {
                    parametro.put("SUBREPORT_DIR", "/desarrollo/sio/reportes/");
                    if (!parametro.isEmpty() || parametro.isEmpty()) {
                        //System.out.println(parametro.get("FECHAINI"));
                        //System.out.println(parametro.get("FECHAFIN"));
                        //System.out.println("Reporte: " + clssRutinas.Reportpath + reporte);
                        this.jasperReport = JasperCompileManager.compileReport("/desarrollo/sio/reportes/" + reporte);
                        //System.out.println("SALIDA: " + reporte.replace(".jrxml", ".pdf"));
                        this.JPrint = JasperFillManager.fillReport(this.jasperReport, parametro, cnn);

                        java.util.Date fechareporte = new java.util.Date();
                        String tmpfecha = new java.text.SimpleDateFormat("yyyy-MM-dd").format(fechareporte);

                        //JasperExportManager.exportReportToPdfFile(JPrint, "/tmp/" + reporte.replace(".jrxml", tmpfecha + ".pdf"));
                        JasperExportManager.exportReportToPdfFile(JPrint, "/desarrollo/"+nombrepdf);

                    } else {
                        //clssRutinas.msgbox("Parametros vacios",1);
                    }
                //}
                //pg.cnn.close();
            }
        } catch (JRException e) {
            System.out.println(e.toString());
            System.out.println("Error de Reporte "+reporte+": "+e.toString());
        }
    }
    
    public void generarreportejrd(Map parametro,String reporte){
        try{            
            if(!reporte.isEmpty() || !reporte.equals("")){
                    if(!parametro.isEmpty()){
                        this.jasperReport = JasperCompileManager.compileReport(reporte);
                        //JPrint = JasperFillManager.fillReport(jasperReport, parametro, con.cnn);
                        JPrint = JasperFillManager.fillReport(jasperReport, parametro, new JREmptyDataSource());
                        JasperExportManager.exportReportToPdfFile(JPrint, "/proyecto/pruebaVentas.pdf");
                        //jv = new JasperViewer(JPrint, false);
                        //jv.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
                        //jv.show();   
                    }else{
                        System.out.println("Parametros vacios");
                    }  
            }else System.out.println("Debe ingresar el query, correctamente");
        }catch(JRException e){
            System.out.println("Error de Reporte "+reporte+": "+e.toString());
        }
    }
}
