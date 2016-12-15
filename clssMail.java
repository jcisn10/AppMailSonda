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
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 *
 * @author Jorge Cisneros (jcisneros.cisneros250@gmail.com)
 */
public class clssMail {

    String smtp = "", puerto = "", usuario = "", clave = "";
    String seguridad = "", autenticacion = "", shares = "";
    String asunto = "", mensaje = "", msgtext = "", para = "", cco = "", bcc = "";
    String MSGCANTIDADES = "";
    String adjuntos[] = null, rpts[] = null;
    String mes = "";
    String ips[][] = null;
    Properties prt = new Properties();
    java.util.Date fechaini = null, fechafin = null;
    clssConexion con = new clssConexion();
    String SQL = "", SERVER = "";
    String SUCURSALES = "";
    int idsucursal = 0;
    String meses[] = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
    SimpleDateFormat parseFecha = new SimpleDateFormat("yyyy-MM-dd");

    public clssMail() {
        FileInputStream finpt = null;
        try {
            finpt = new java.io.FileInputStream("configmail.properties");
            prt.load(finpt);
            smtp = prt.getProperty("SMTP");
            puerto = prt.getProperty("PUERTO");
            seguridad = prt.getProperty("SEGURIDADMAIL", "true");
            usuario = prt.getProperty("USUARIO");
            autenticacion = prt.getProperty("AUTENTICACION", "true");
            clave = con.desencrypta(prt.getProperty("CLAVE"));
            idsucursal = Integer.parseInt(prt.getProperty("SUCURSAL"));
            SERVER = prt.getProperty("SERVER");
//            if (prt.getProperty("FECHAINI") != null) {
//                fechaini = parseFecha.parse(prt.getProperty("FECHAINI"));
//            }
//            if (prt.getProperty("FECHAINI") != null) {
//                fechafin = parseFecha.parse(prt.getProperty("FECHAFIN"));
//            }
            int filas = 0, cols = 0;
            SUCURSALES = prt.getProperty("SERVIDORES");
            filas = prt.getProperty("SERVIDORES").split(";").length;
            cols = prt.getProperty("SERVIDORES").split(";")[0].split(",").length;
            ips = new String[filas][cols];
            //ips = prt.getProperty("SERVIDORES");
            /*for (int i = 0; i < filas; i++) {
             System.arraycopy(prt.getProperty("SERVIDORES").split(";")[i].split(","), 0, ips[i], 0, cols);
             }*/
            /*for (int i = 0; i < filas; i++) {
             for (int j = 0; j < cols; j++) {
             System.out.print(ips[i][j] + "\t");
             }
             System.out.println();
             }*/
        } catch (IOException | NumberFormatException e) {
        }
    }

    public void setParametros() {

        prt.put("mail.smtp.host", smtp);
        prt.setProperty("mail.smtp.ssl.trust", smtp);
        prt.setProperty("mail.smtp.starttls.enable", seguridad);
        prt.setProperty("mail.smtp.port", puerto);
        prt.setProperty("mail.smtp.user", usuario);
        prt.setProperty("mail.smtp.auth", autenticacion);

        //prt.put("mail.debug", "true");
        //prt.put("mail.smtp.socketFactory.port", SMTP_PORT);  
        //prt.put("mail.smtp.socketFactory.class", SSL_FACTORY);  
        //prt.put("mail.smtp.socketFactory.fallback", "false");
        /*Session session = Session.getDefaultInstance(props,  
         new javax.mail.Authenticator() {  
         protected PasswordAuthentication getPasswordAuthentication() {  
         return new PasswordAuthentication("yourMaidID",  
         "yourPassword");  
         }  
         });  
  
         session.setDebug(debug);*/
    }

    public void SetFechas(Date inicio, Date fin) {
        this.fechaini = inicio;
        this.fechafin = fin;
    }

    public void getInfo(java.util.Date fechaini, java.util.Date fechafin, String mes) {
        String salto = "<br />";
        String msgeneral = "";
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
        java.text.SimpleDateFormat sformato = new java.text.SimpleDateFormat("yyyy-MM-dd");

        this.mes = meses[calendar.get(Calendar.MONTH)];

        this.msgtext = "<html><head></head>";
        this.msgtext += "<body>";

        this.msgtext = "<h1>LofaTrading S.A</h1>";
        this.msgtext += "<img src=\"http://www.opticalopez.net/wp-content/uploads/2013/08/LOGO-OL-4.jpg\" border=\"0\" style=\"width: 150px;\"><br>";
        this.msgtext += "<i>...algo nuevo que ver</i>";
        this.msgtext += "<center>";
        this.msgtext += "<br><p>" + this.mensaje + " del mes de " + this.mes + "</p>";
        this.msgtext += "<br><p>Del " + sformato.format(fechaini) + "   al   " + sformato.format(fechafin) + "</p><br>";

        int m = 0;

        int sucursales[] = new int[SUCURSALES.split(",").length];

        for (int r = 0; r < SUCURSALES.split(",").length; r++) {
            sucursales[r] = Integer.parseInt(SUCURSALES.split(",")[r]);
        }

        Object datos[][] = new Object[12][sucursales.length];

        String campos = "Id,Sucursal,Aros Mes,Aros D&iacute;a,Facturas Mes,Facturas d&iacute;a,&Oacute;rdenes Mes,&Oacute;rdenes d&iacute;a,Subtotal,Descuentos,Itbms,Total,Total sin Itbm";
//        String campos = "Id,Sucursal,Subtotal,Descuentos,Itbms,Total,Total sin Itbm";
        String titulos[] = campos.split(",");

        this.msgtext += "<table id=\"tablaVentas\" border=\"0\" cellspacing=\"1\" cellpading=\"0\" class=\"gridtable\" style=\"border: 1px solid #CCC; padding: 1px;\">";
        this.msgtext += "<tr colspan=13 style=\"padding: 3px 5px;\"><b>Totales de Ventas</b></th>";
        this.msgtext += "<thead style=\"background-color: silver;\">";
        for (String titulo : titulos) {
            this.msgtext += "<th style=\"padding: 3px 5px;\"><b> " + titulo + " </b></th>";
        }
        this.msgtext += "</thead>";

        con.cnnconexiongp(SERVER);
        con.crearStatement();
        int campossql = 10,contador = 0;
        this.SQL = "select nidsucursal,a.titulo sucursal,n_arosmes ,n_arosdia ,n_facturasmes ,n_facturasdia ,n_devmes ,n_devdia ,n_ordenmes ,n_ordendia ";
        this.SQL += "from otl_sucursal a,fn_aros_vtas('" + sformato.format(fechaini) + "','"+sformato.format(fechafin)+"') b ";
        this.SQL += "Where a.idsucursal=b.nidsucursal ";
        MSGCANTIDADES += "<center><table id=\"tablaVentas\" border=\"0\" cellspacing=\"1\" cellpading=\"0\" class=\"gridtable\" style=\"border: 1px solid #CCC; padding: 1px;\">";
        String camposcant = "ID,Sucursal,aros facturados x mes,aros facturados x dia,facturas x mes,facturas x dia,dev. de aros x mes,dev. de aros x dia,ordenes x mes,ordenes x dia";
        String header[] = camposcant.split(",");
        this.MSGCANTIDADES += "<tr colspan=10 style=\"padding: 3px 5px;\"><b> Cantidades Totales </b></th>";
        this.MSGCANTIDADES += "<thead style=\"background-color: silver;\">";
        for (String titulo : header) {
            this.MSGCANTIDADES += "<th style=\"padding: 3px 5px;\"><b> " + titulo + " </b></th>";
        }
        this.MSGCANTIDADES += "</thead>";
        
        con.execSQL(SQL);
        try {
            String nomsucursal = "";
            while(con.rs.next()){
                contador = 0;
                String color = ((con.rs.getInt("nidsucursal") + 1) % 2) == 0 ? "background-color: #e1e1e1;" : "";
                MSGCANTIDADES += "<tr>";
                while (contador < campossql) {
                    contador++;
                    if (contador == 2){
                        nomsucursal = con.rs.getString(contador);
                        nomsucursal = nomsucursal.replaceAll("í", "&iacute;");
                        nomsucursal = nomsucursal.replaceAll("ó", "&oacute;");
                        nomsucursal = nomsucursal.replaceAll("ñ", "&ntilde;");
                        System.out.println(nomsucursal);
                        MSGCANTIDADES += "<td  style=\"border-bottom:1px solid #ccc;border-right:1px solid #ccc;padding: 3px 5px;text-align: center;" + color + "\"><i>" + nomsucursal + "</i></td>";
                    }else{
                        MSGCANTIDADES += "<td  style=\"border-bottom:1px solid #ccc;border-right:1px solid #ccc;padding: 3px 5px;text-align: center;" + color + "\">" + con.rs.getObject(contador) + "</td>";
                    }
                }
                MSGCANTIDADES += "</tr>";
            }
        } catch (SQLException ex) {
            Logger.getLogger(clssMail.class.getName()).log(Level.SEVERE, null, ex);
        }
        MSGCANTIDADES += "</table></center>";
        
        for (int sucursal = 0; sucursal < sucursales.length; sucursal++) {
            this.SQL = "SELECT * FROM ";
//            this.SQL = "SELECT f_sucursal,subtotal,desctos,itbms,total,totalsinitbm FROM ";
            this.SQL += "(select f_sucursal,f_totalvtasaros,f_cantidadarosfactura,f_cantidadfacturas,f_cantidadfacturasdia ";
            this.SQL += ",f_cantidadordenesmes,f_cantidadordenesdia ";
            this.SQL += "from fn_consulta_vtas_mes_oscar(" + sucursales[sucursal] + ",'" + sformato.format(fechaini) + "','" + sformato.format(fechafin) + "')) as X ";
            this.SQL += ",(Select ";
            this.SQL += "(subtotal_fac - subtotal_dev) subtotal";
            this.SQL += ",(desctos_fac - desctos_dev) desctos";
            this.SQL += ",(itbms_fac - itbms_dev) itbms ";
            this.SQL += ",(total_fac - total_dev) total";
            this.SQL += ",(totalsinitbm_fac - totalsinitbm_dev) totalsinitbm ";
            this.SQL += "From ";
            this.SQL += "(Select coalesce(sum(total_total),0.00) subtotal_dev";
            this.SQL += ",coalesce(sum(total_descuento+total_otros_descuento),0.00) desctos_dev,coalesce(sum(itbms),0.00) itbms_dev,coalesce(sum(total),0.00) total_dev";
            this.SQL += ",coalesce((sum(total)-sum(itbms)),0.00) totalsinitbm_dev ";
            this.SQL += "From fn_consulta_reporte_devoluciones(" + sucursales[sucursal] + ") ";
            this.SQL += "where (fecha>='" + sformato.format(fechaini) + "' and fecha<='" + sformato.format(fechafin) + "')) As A";
            this.SQL += ",(Select sum(subtotales) subtotal_fac";
            this.SQL += ",sum(descuentoley+descuento_otros) desctos_fac";
            this.SQL += ",sum(itbm) itbms_fac,sum(totals) total_fac ";
            this.SQL += ",(sum(totals)-sum(itbm)) totalsinitbm_fac ";
            this.SQL += "From fn_ventas_desglose(" + sucursales[sucursal] + ",'" + sformato.format(fechaini) + "','" + sformato.format(fechafin) + "')) As B) As Y";

            if (con.execSQL(this.SQL)) {
                try {
                    String color = ((sucursal+1) % 2) == 0 ? "background-color: #e1e1e1;" : "";
                    this.msgtext += "<tr>";
                    if (con.rs.next()) {
                        this.msgtext += "<td  style=\"border-bottom:1px solid #ccc;border-right:1px solid #ccc;padding: 3px 5px;text-align: center;" + color + "\"><b>" + sucursales[sucursal] + "</b></td>";
                        this.msgtext += "<td nowrap style=\"border-bottom:1px solid #ccc;border-right:1px solid #ccc;padding: 3px 5px;text-align: center;" + color + "\">" + con.rs.getString(1).replaceAll("í", "&iacute;").replaceAll("ó", "&oacute;").replaceAll("ñ", "&ntilde;") + "</td>";
                        this.msgtext += "<td style=\"border-bottom:1px solid #ccc;border-right:1px solid #ccc;padding: 3px 5px;text-align: center;" + color + "\">" + con.rs.getObject(2) + "</td>";
                        this.msgtext += "<td style=\"border-bottom:1px solid #ccc;border-right:1px solid #ccc;padding: 3px 5px;text-align: center;" + color + "\">" + con.rs.getObject(3) + "</td>";
                        this.msgtext += "<td style=\"border-bottom:1px solid #ccc;border-right:1px solid #ccc;padding: 3px 5px;text-align: center;" + color + "\">" + con.rs.getObject(4) + "</td>";
                        this.msgtext += "<td style=\"border-bottom:1px solid #ccc;border-right:1px solid #ccc;padding: 3px 5px;text-align: center;" + color + "\">" + con.rs.getObject(5) + "</td>";
                        this.msgtext += "<td style=\"border-bottom:1px solid #ccc;border-right:1px solid #ccc;padding: 3px 5px;text-align: center;" + color + "\">" + con.rs.getObject(6) + "</td>";
                        this.msgtext += "<td style=\"border-bottom:1px solid #ccc;border-right:1px solid #ccc;padding: 3px 5px;text-align: center;" + color + "\">" + con.rs.getObject(7) + "</td>";
                        this.msgtext += "<td style=\"border-bottom:1px solid #ccc;border-right:1px solid #ccc;padding: 3px 5px;text-align: right;" + color + "\">" + (new java.text.DecimalFormat("###,###,##0.00").format(con.rs.getDouble(8))) + "</td>";
                        this.msgtext += "<td style=\"border-bottom:1px solid #ccc;border-right:1px solid #ccc;padding: 3px 5px;text-align: right;" + color + "\">" + (new java.text.DecimalFormat("###,###,##0.00").format(con.rs.getDouble(9))) + "</td>";
                        this.msgtext += "<td style=\"border-bottom:1px solid #ccc;border-right:1px solid #ccc;padding: 3px 5px;text-align: right;" + color + "\">" + (new java.text.DecimalFormat("###,###,##0.00").format(con.rs.getDouble(10))) + "</td>";
                        this.msgtext += "<td style=\"border-bottom:1px solid #ccc;border-right:1px solid #ccc;padding: 3px 5px;text-align: right;" + color + "\">" + (new java.text.DecimalFormat("###,###,##0.00").format(con.rs.getDouble(11))) + "</td>";
                        this.msgtext += "<td style=\"border-bottom:1px solid #ccc;padding: 2px 2px;text-align: right;" + color + "\"><b><font color=blue>" + (new java.text.DecimalFormat("###,###,##0.00").format(con.rs.getDouble(12))) + "</font></b></td>";
                    }
                    this.msgtext += "</tr>";
                    System.out.println("Sucursal ====> " + sucursales[sucursal]);
                } catch (SQLException e) {
                }
            }
        }//Fin de For

        /* this.SQL = "select sum(f_subtotal) subtotal,sum(f_desctos) desctos,sum(f_itbms) itbms,sum(f_total) total,sum(f_totalsinitbm) totalsinitbm ";
         this.SQL += "from fn_total_vtas('2,3,7,11,14,15','" + sformato.format(fechaini) + "','" + sformato.format(fechafin) + "');";
         if (con.execSQL(this.SQL)) {
         try {
         msgeneral += "<center><table border=1 class=\"gridtable\">";
         msgeneral += "<tr><td colspan=5><p align=\"center\"><font color=red>General</font></p></td></tr><tr>";
         msgeneral += "<tr><td>Subtotal</td><td>Descuentos</td><td>Itbms</td><td>Total</td><td>Total Sin Itbms</td></tr><tr>";
         if (con.rs.next()) {
         msgeneral += "<td><i>" + (new java.text.DecimalFormat("###,###,##0.00").format(con.rs.getDouble(1))) + "</i></td>";
         msgeneral += "<td><i>" + (new java.text.DecimalFormat("###,###,##0.00").format(con.rs.getDouble(2))) + "</i></td>";
         msgeneral += "<td><i>" + (new java.text.DecimalFormat("###,###,##0.00").format(con.rs.getDouble(3))) + "</i></td>";
         msgeneral += "<td><i>" + (new java.text.DecimalFormat("###,###,##0.00").format(con.rs.getDouble(4))) + "</i></td>";
         msgeneral += "<td><i>" + (new java.text.DecimalFormat("###,###,##0.00").format(con.rs.getDouble(5))) + "</i></td>";
         }
         msgeneral += "</tr></table></center>";
         } catch (SQLException e) {

         }
         }*/
        con.cerrarConexion();

        this.msgtext = this.msgtext + "</table></center> <br/>" + msgeneral;
        this.msgtext += "</html>";
        //System.out.println(this.msgtext);
    }

    public void getInformacion(java.util.Date fechaini, java.util.Date fechafin, String mes) {
        try {
            String salto = "<br />";
            //String td = "<td>";
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH));

            this.mes = meses[calendar.get(Calendar.MONTH)];

            this.msgtext = "<html><h1>LofaTrading S.A</h1>";
//            this.msgtext += "<h2><font color=red>Optica Lopez</font></h2><hr/>";
            this.msgtext += "<img src=\"http://www.opticalopez.net/wp-content/uploads/2013/08/LOGO-OL-4.jpg\" border=\"0\" style=\"width: 150px;\"><br>";
            this.msgtext += "<center>";
            this.msgtext += "<br><p>" + this.mensaje + " del mes de " + this.mes + "</p><br>";

            Object datos[][] = new Object[ips.length + 1][14];

            java.text.SimpleDateFormat sformato = new java.text.SimpleDateFormat("yyyy-MM-dd");

            int m = 0;
            for (int i = 0; i < datos.length; i++) {
                m = 0;
                if (i == 0) {
                    datos[i][m] = "<td><b>Sucursal</b></td>";
                    m++;
                    datos[i][m] = "<td>Cantidad de Aros vendidos en " + mes + "</td>";
                    m++;
                    datos[i][m] = "<td>Cantidad de Aros del " + sformato.format(fechafin) + "</td>";
                    m++;
                    datos[i][m] = "<td>Cantidad de Facturas de " + mes + "</td>";
                    m++;
                    datos[i][m] = "<td>Cantidad de Facturas del " + sformato.format(fechafin) + "</td>";
                    m++;
                    datos[i][m] = "<td>Cantidad de Devoluciones (Aros)</td>";
                    m++;
                    datos[i][m] = "<td>Cantidad de Ordenes del mes de " + mes + "</td>";
                    m++;
                    datos[i][m] = "<td>Cantidad de Ordenes del " + sformato.format(fechafin) + "</td>";
                    m++;
                    //datos[i][m] = "<td>Total Devoluciones: " + sformato.format(fechafin) + "</td>";
                    //m++;
                    datos[i][m] = "<td>----------------------------------------------------</td>";
                    m++;
                    datos[i][m] = "<td>SubTotal: " + sformato.format(fechafin) + "</td>";
                    m++;
                    datos[i][m] = "<td>Total DESC: " + sformato.format(fechafin) + "</td>";
                    m++;
                    datos[i][m] = "<td><b><i>Total Sin ITBM: " + sformato.format(fechafin) + "</i></b></td>";
                    m++;
                    datos[i][m] = "<td>Total ITBM: " + sformato.format(fechafin) + "</td>";
                    m++;
                    datos[i][m] = "<td>Total Con ITBM: " + sformato.format(fechafin) + "</td>";
                } else {
                    if (con.cnnconexiongp(ips[i - 1][0])) {
                        this.idsucursal = Integer.parseInt(ips[i - 1][1]);
                        if (con.crearStatement()) {
                            SQL = "Select titulo as sucursal From otl_sucursal Where idsucursal=" + this.idsucursal;
                            if (con.execSQL(SQL)) {
                                if (con.rs.next()) {
                                    datos[i][m] = "<td><b><i>" + con.rs.getString("sucursal") + "</i></b></td>";
                                }
                            }
                            m++;
                            this.SQL = "Select cast(sum(cant_dev + cant_fac) as int) From ";
                            this.SQL += "(Select - coalesce(sum(b.cantidad),0) cant_dev From otl_devoluciones a,otl_devolucionitems b ";
                            this.SQL += "where (fecha >= '" + sformato.format(fechaini) + "' and fecha <= '" + sformato.format(fechafin) + "') ";
                            this.SQL += "and a.idseqdevoluciones = b.idseqdevoluciones ";
                            this.SQL += "and (b.codigo ilike 'a%' and b.codigo != 'A0000') ";
                            this.SQL += "And a.idsucursal=" + this.idsucursal;
                            this.SQL += "And a.idsucursal=b.idsucursal ";
                            this.SQL += "And b.cantidad > 0) As A  ";
                            this.SQL += ",";
                            this.SQL += "(Select coalesce(sum(b.cantidad),0) cant_fac From otl_factura a,otl_facturadet b ";
                            this.SQL += "where (fecha >= '" + sformato.format(fechaini) + "' and fecha <= '" + sformato.format(fechafin) + "') ";
                            this.SQL += "and a.idseqfactura = b.idseqfactura ";
                            this.SQL += "and (b.codigo ilike 'a%' and b.codigo != 'A0000') ";
                            this.SQL += "And a.idsucursal=" + this.idsucursal;
                            this.SQL += "And a.idsucursal=b.idsucursal ";
                            this.SQL += "And b.cantidad > 0) As B ";
                            if (con.execSQL(SQL)) {
                                if (con.rs.next()) {
                                    datos[i][m] = "<td>" + new java.text.DecimalFormat("#,##0").format(con.rs.getInt(1)) + "</td>";
                                }
                            }
                            m++;

                            this.SQL = "Select cast(sum(cant_dev + cant_fac) as int) From ";
                            this.SQL += "(Select - coalesce(sum(b.cantidad),0) cant_dev From otl_devoluciones a,otl_devolucionitems b ";
                            this.SQL += "where (fecha >= '" + sformato.format(fechafin) + "' and fecha <= '" + sformato.format(fechafin) + "') ";
                            this.SQL += "and a.idseqdevoluciones = b.idseqdevoluciones ";
                            this.SQL += "and (b.codigo ilike 'a%' and b.codigo != 'A0000') ";
                            this.SQL += "And a.idsucursal=" + this.idsucursal;
                            this.SQL += "And a.idsucursal=b.idsucursal ";
                            this.SQL += "And b.cantidad > 0) As A  ";
                            this.SQL += ",";
                            this.SQL += "(Select coalesce(sum(b.cantidad),0) cant_fac From otl_factura a,otl_facturadet b ";
                            this.SQL += "where (fecha >= '" + sformato.format(fechafin) + "' and fecha <= '" + sformato.format(fechafin) + "') ";
                            this.SQL += "and a.idseqfactura = b.idseqfactura ";
                            this.SQL += "and (b.codigo ilike 'a%' and b.codigo != 'A0000') ";
                            this.SQL += "And a.idsucursal=" + this.idsucursal;
                            this.SQL += "And a.idsucursal=b.idsucursal ";
                            this.SQL += "And b.cantidad > 0) As B ";
                            if (con.execSQL(SQL)) {
                                if (con.rs.next()) {
                                    datos[i][m] = "<td>" + new java.text.DecimalFormat("#,##0").format(con.rs.getInt(1)) + "</td>";
                                }
                            }
                            m++;

                            SQL = "select count(1) from otl_factura where (fecha >= '" + sformato.format(fechaini) + "' and fecha <= '" + sformato.format(fechafin) + "') ";
                            SQL += "And idsucursal=" + this.idsucursal;
                            if (con.execSQL(SQL)) {
                                if (con.rs.next()) {
                                    datos[i][m] = "<td>" + con.rs.getInt(1) + "</td>";
                                }
                            }
                            m++;

                            SQL = "select count(1) from otl_factura where fecha = '" + sformato.format(fechafin) + "' ";
                            SQL += "And idsucursal=" + this.idsucursal;
                            if (con.execSQL(SQL)) {
                                if (con.rs.next()) {
                                    datos[i][m] = "<td>" + con.rs.getInt(1) + "</td>";
                                }
                            }
                            m++;

                            SQL = "select count(1) ";
                            SQL += "from otl_devoluciones a,otl_factura b,otl_facturadet c  ";
                            SQL += "where (a.fecha >= '" + sformato.format(fechaini) + "' and a.fecha <= '" + sformato.format(fechafin) + "') ";
                            SQL += "and b.idseqfactura = c.idseqfactura ";
                            SQL += "and a.idseqfactura = b.idseqfactura ";
                            SQL += "and (c.codigo ilike 'A%' and c.codigo not like 'A0000') ";
                            SQL += "and b.estado = 'D' ";
                            SQL += "And a.idsucursal=" + this.idsucursal;
                            SQL += " And a.idsucursal=b.idsucursal And b.idsucursal=c.idsucursal ";
                            if (con.execSQL(SQL)) {
                                if (con.rs.next()) {
                                    datos[i][m] = "<td>" + con.rs.getInt(1) + "</td>";
                                }
                            }
                            m++;

                            SQL = "select ";
                            SQL += "count(1) ";
                            SQL += "From ";
                            SQL += "otl_solorordenproduccion a ";
                            SQL += ",otl_solordenproducciondet b ";
                            SQL += "where (a.fecha >= '" + sformato.format(fechaini) + "' and a.fecha <= '" + sformato.format(fechafin) + "') ";
                            SQL += "and a.para = 1 ";
                            SQL += "and a.estado != 'A' ";
                            SQL += "and a.idseqsolorden = b.idseqsolordern ";
                            SQL += "and (b.codigo ilike 'A%') ";
                            SQL += "And a.idsucursal=" + this.idsucursal;
                            SQL += " And a.idsucursal=b.idsucursal ";
                            if (con.execSQL(SQL)) {
                                if (con.rs.next()) {
                                    datos[i][m] = "<td>" + con.rs.getInt(1) + "</td>";
                                }
                            }
                            m++;

                            SQL = "select ";
                            SQL += "count(1) ";
                            SQL += "From ";
                            SQL += "otl_solorordenproduccion a ";
                            SQL += ",otl_solordenproducciondet b ";
                            SQL += "where a.fecha = '" + sformato.format(fechafin) + "' ";
                            SQL += "and a.para = 1 ";
                            SQL += "and a.estado != 'A' ";
                            SQL += "and a.idseqsolorden = b.idseqsolordern ";
                            SQL += "and (b.codigo ilike 'A%') ";
                            SQL += "And a.idsucursal=" + this.idsucursal;
                            SQL += " And a.idsucursal=b.idsucursal ";
                            if (con.execSQL(SQL)) {
                                if (con.rs.next()) {
                                    datos[i][m] = "<td>" + con.rs.getInt(1) + "</td>";
                                }
                            }
                            m++;

                            double totdev = 0.00;
                            double totdev_itbm = 0.00, totdev_desctos = 0.00, totdev_subtotal = 0.00, totdev_sinitbm = 0.0;
                            double totsinitbm = 0.00, totitbm = 0.00;
                            double totconitbm = 0.00, totdesctos = 0.00;

                            this.SQL = "Select sum(total_total) subtotal,sum(total_descuento+total_otros_descuento) desctos";
                            this.SQL += ",sum(total_sin_itbm) total_sin_itbm,sum(itbms) itbms,sum(total) total ";
                            this.SQL += "From fn_consulta_reporte_devoluciones(" + this.idsucursal + ") ";
                            this.SQL += "where (fecha>='" + sformato.format(fechaini) + "' and fecha<='" + sformato.format(fechafin) + "')";
                            if (con.execSQL(this.SQL)) {
                                if (con.rs.next()) {
                                    totdev_subtotal = con.rs.getDouble("subtotal");
                                    totdev_desctos = con.rs.getDouble("desctos");
                                    totdev_sinitbm = con.rs.getDouble("total_sin_itbm");
                                    totdev_itbm = con.rs.getDouble("itbms");
                                    totdev = con.rs.getDouble("total");
                                }
                            }

                            this.SQL = "Select sum(subtotales) subtotales,sum(descuentoley+descuento_otros) totaldesc,sum(itbm) totalitbms,sum(totals) totales,(sum(totals)-sum(itbm)) totalsinitbm  ";
                            this.SQL += "From fn_ventas_desglose(" + this.idsucursal + ",'" + sformato.format(fechaini) + "','" + sformato.format(fechafin) + "')";
                            if (con.execSQL(SQL)) {
                                if (con.rs.next()) {
                                    totsinitbm = con.rs.getDouble("totalsinitbm");
                                    totdesctos = con.rs.getDouble("totaldesc");
                                    totitbm = con.rs.getDouble("totalitbms");
                                }
                            }

                            totsinitbm = totsinitbm - totdev_sinitbm;
                            //totdesctos = totdesctos - totdev_desctos;
                            totitbm = totitbm - totdev_itbm;

                            //totitbm = (totsinitbm - totdesctos) * 0.07;
                            totconitbm = (totsinitbm) - (totdesctos) + (totitbm);
                            //datos[i][m] = "<td>" + (new java.text.DecimalFormat("###,###,##0.00").format(totdev)) + "</td>";//SubTotales
                            //m++;
                            datos[i][m] = "<td>-----------------</td>";//SubTotales
                            m++;
                            datos[i][m] = "<td>" + (new java.text.DecimalFormat("###,###,##0.00").format(totsinitbm)) + "</td>";//SubTotales
                            m++;
                            datos[i][m] = "<td>" + (new java.text.DecimalFormat("###,###,##0.00").format(totdesctos)) + "</td>";//Descuentos
                            m++;
                            //datos[i][m] = "<td><b><i>" + (new java.text.DecimalFormat("###,###,##0.00").format((totsinitbm) - (totdesctos))) + "</i></b></td>";//Totales Sin ITbms
                            datos[i][m] = "<td><b><i>" + (new java.text.DecimalFormat("###,###,##0.00").format((totsinitbm) - (totdesctos))) + "</i></b></td>";//Totales Sin ITbms
                            m++;
                            datos[i][m] = "<td>" + (new java.text.DecimalFormat("###,###,##0.00").format(totitbm)) + "</td>";//Itbms
                            m++;
                            datos[i][m] = "<td>" + (new java.text.DecimalFormat("###,###,##0.00").format(totconitbm)) + "</td>";//Totales

                            totsinitbm = 0.00;
                            totdesctos = 0.00;
                            totitbm = 0.00;
                            totconitbm = 0.00;

                        }
                        con.cerrarConexion();
                    }
                }
            }
            this.msgtext += "<table border=1>";
            for (int i = 0; i < datos[0].length; i++) {
                this.msgtext += "<tr>";
                for (Object[] dato : datos) {
                    this.msgtext += dato[i];
                }
                this.msgtext += "</tr>";
            }
            this.msgtext += "</table>";
            this.msgtext += "</center>";

        } catch (NumberFormatException | SQLException e) {
            //e.printStackTrace();
        }
    }

    public void setInformacionMail() {
        shares = prt.getProperty("ADJUNTOS");
        this.para = prt.getProperty("PARA");
        this.cco = prt.getProperty("CCO");
        this.bcc = prt.getProperty("BCC");

        this.asunto = prt.getProperty("ASUNTO");
        this.mensaje = prt.getProperty("MENSAJE");
        this.adjuntos = shares.split(";");
    }

    public void generarreporte() {

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
        this.mes = meses[calendar.get(Calendar.MONTH)];

        Calendar cal2 = Calendar.getInstance();
        cal2.set(Calendar.DAY_OF_MONTH, cal2.get(Calendar.DAY_OF_MONTH));
        cal2.set(Calendar.MONTH, cal2.get(Calendar.MONTH));
        cal2.set(Calendar.YEAR, cal2.get(Calendar.YEAR));

        //calendar.set(Calendar.MINUTE, 59);
        //calendar.set(Calendar.SECOND, 59);
        //calendar.set(Calendar.MILLISECOND, 999);
        clssIreports rpt = new clssIreports();
        if (fechaini == null) {
            this.fechaini = calendar.getTime();
        }
        if (fechafin == null) {
            this.fechafin = cal2.getTime();
        }

        //this.getInformacion(this.fechaini, this.fechafin, mes);
        this.getInfo(this.fechaini, this.fechafin, mes);
        this.getInfoOrdenes(fechafin);
        //java.util.Date fechaini = new java.util.Date();        
        //java.util.Date fechafin = new java.util.Date();

        //System.out.println(sformato.format(fechaini));
        //System.out.println(sformato.format(fechafin));
        /*System.out.println(this.idsucursal);
         rpt.parameter.put("FECHAINI", this.fechaini);
         rpt.parameter.put("FECHAFIN", this.fechafin);
         rpt.parameter.put("SUCURSAL", this.idsucursal);
         for(int i=0;i<this.adjuntos.length;i++){
         rpt.generarreporte(rpt.parameter, this.adjuntos[i]);
         }*/
    }

    public boolean SendMail() {
        boolean status = false;
        try {
            int cant_to_email = 0;
            String to[] = this.para.split(",");
            cant_to_email = to.length;
            //Address to_email[] = new InternetAddress(this.para);
            InternetAddress[] to_email = new InternetAddress[cant_to_email];
            for (int r = 0; r < cant_to_email; r++) {
                to_email[r] = new InternetAddress(to[r]);
            }

            InternetAddress cco_email[] = null;
            InternetAddress bcc_email[] = null;

            Session session = Session.getDefaultInstance(prt, null);
            // session.setDebug(true);

            // Se compone la parte del texto
            BodyPart texto = new MimeBodyPart();
//            texto.setContent(this.msgtext+this.MSGCANTIDADES, "text/html");
            texto.setContent(this.msgtext, "text/html");

            String fichero = "";
            String linea;
            BufferedReader br = new BufferedReader(
                    new FileReader(prt.getProperty("FIRMA")));
            while ((linea = br.readLine()) != null) {
                fichero += linea;
            }
            br.close();

            // Una MultiParte para agrupar texto e imagen.
            MimeMultipart multiParte = new MimeMultipart();

            multiParte.addBodyPart(texto);

//            clssConexion.db = "laboratorio";
//            clssConexion.servidor = "localhost";
//            clssConexion.usuario = "desarrollo";
//            clssConexion.clave = "123456";
//
//            con.cnnconexiongp("localhost");
//            try {
//                con.cnn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/laboratorio","desarrollo","123456");
//            } catch (SQLException ex) {
//                Logger.getLogger(clssMail.class.getName()).log(Level.SEVERE, null, ex);
//            }
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) - 90);
            calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
            calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR));

            Calendar cal2 = Calendar.getInstance();
            cal2.set(Calendar.DAY_OF_MONTH, cal2.get(Calendar.DAY_OF_MONTH));
            cal2.set(Calendar.MONTH, cal2.get(Calendar.MONTH));
            cal2.set(Calendar.YEAR, cal2.get(Calendar.YEAR));

            java.util.Date FECHAINI = calendar.getTime();
            java.util.Date FECHAFIN = cal2.getTime();

//            clssIreports rpt = new clssIreports();
//            rpt.parameter.put("fecha_desde", FECHAINI);
//            //rpt.parameter.put("fecha", FECHAFIN);
//            rpt.generarreporte(rpt.parameter, "OrdenesAcumuladas.jrxml", "Ventas1.pdf", true, con.cnn);
//            rpt.generarreporte(rpt.parameter, "ordenesLaboratorio.jrxml", "Ventas2.pdf", true, con.cnn);
//            try {
//                con.cnn.close();
//            } catch (SQLException ex) {
//                Logger.getLogger(clssMail.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            
//            File archivo = null;
//            javax.activation.DataSource datasrc = null;
//            for (int i = 0; i < this.adjuntos.length; i++) {
//             //System.out.println(this.adjuntos[i]);
//                //Conversion de archivos *.jrxml a *.pdf
////                archivo = new File(this.adjuntos[i].replace(".jrxml", ".pdf"));
//                archivo = new File("/desarrollo/Reporte.pdf");
//                datasrc = new FileDataSource(archivo);
//                System.out.println(archivo);
//                //BodyPart adjunto = new MimeBodyPart();
//                MimeBodyPart adjunto = new MimeBodyPart();
//                adjunto.setDataHandler(new DataHandler(datasrc));
//                adjunto.setFileName(archivo.getName());
//                multiParte.addBodyPart(adjunto);
//            }
            
            File archivo = null;
            javax.activation.DataSource datasrc = null;
            archivo = new File("/opt/publico/Ventas_Lic/ventas_" + new java.text.SimpleDateFormat("yyyy-MM-dd").format(FECHAFIN) + ".csv");
            if(archivo.exists()){
                datasrc = new FileDataSource(archivo);
                MimeBodyPart adjunto = new MimeBodyPart();
                adjunto.setDataHandler(new DataHandler(datasrc));
                adjunto.setFileName(archivo.getName());
                multiParte.addBodyPart(adjunto);
            }
            
            BodyPart firma = new MimeBodyPart();
            firma.setContent(fichero, "text/html");
            multiParte.addBodyPart(firma);

            // Se compone el correo, dando to, from, subject y el
            // contenido.
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(usuario));
            //Mensaje TO -- Para
            message.addRecipients(Message.RecipientType.TO, to_email);

            //Mensaje CCO -- Con Copia
            if (!this.cco.isEmpty() && this.cco != null && !this.cco.equals("")) {
                String cc[] = this.cco.split(",");
                cco_email = new InternetAddress[cc.length];
                for (int c = 0; c < cc.length; c++) {
                    cco_email[c] = new InternetAddress(cc[c]);
                }
                message.addRecipients(Message.RecipientType.CC, cco_email);
            }

            //Mensaje BCC -- Con Copia Oculta
            if (!this.bcc.isEmpty() && this.bcc != null && !this.bcc.equals("")) {
                String bcco[] = this.bcc.split(",");
                bcc_email = new InternetAddress[bcco.length];
                for (int a = 0; a < bcco.length; a++) {
                    bcc_email[a] = new InternetAddress(bcco[a]);
                }
                message.addRecipients(Message.RecipientType.BCC, bcc_email);
            }

//            message.setSubject(this.asunto.concat(" del "+Calendar.getInstance().getTime()));
//            message.setSubject(this.asunto.concat(" del " + new java.text.SimpleDateFormat("dd-MM-yyyy").format(Calendar.getInstance().getTime())));
            message.setSubject(this.asunto.concat(" del **** " + new java.text.SimpleDateFormat("dd-MM-yyyy").format(this.fechafin)));
            System.out.println(this.fechafin);
            message.setContent(multiParte);

            // Se envia el correo.
            Transport t = session.getTransport("smtp");
            t.connect(null, usuario, clave);
            t.sendMessage(message, message.getAllRecipients());
            t.close();
            status = true;
        } catch (IOException | MessagingException e) {
            e.printStackTrace();
            status = false;
        }
        return status;
    }

    public void getInfoOrdenes(java.util.Date fechafin) {
        String msgeneral = "";
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
        java.text.SimpleDateFormat sformato = new java.text.SimpleDateFormat("yyyy-MM-dd");
        this.mes = meses[calendar.get(Calendar.MONTH)];

        con.cnnconexiongp(SERVER);
        con.crearStatement();

        this.SQL = "SELECT * FROM fn_consulta_resumen_ordenes('" + sformato.format(fechafin) + "'::date)";
//        this.SQL = " sformato.format(fechafin) + "')) As B) As Y";

        if (con.execSQL(this.SQL)) {
            this.msgtext += "<center>A continuaci&oacute;n se muestra un resumen de las &oacute;rdenes de las &oacute;pticas</center>";
            this.msgtext += "</br>";
            this.msgtext += "<center><table id=\"tablaVentas\" border=\"0\" cellspacing=\"1\" cellpading=\"0\" class=\"gridtable\" style=\"border: 1px solid #CCC; padding: 1px;\">";
            this.msgtext += "<thead style=\"background-color: silver;\">";
            this.msgtext += "<th style=\"padding: 3px 5px;\">Sucursal</th>";
            this.msgtext += "<th style=\"padding: 3px 5px;\">&Oacute;rdenes <br/>Recibidas</th>";
            this.msgtext += "<th style=\"padding: 3px 5px;\">Tiempo de <br/>Producci&oacute;n en hr</th>";
            this.msgtext += "<th style=\"padding: 3px 5px;\">&Oacute;rdenes en<br/>Gaveta</th>";
            this.msgtext += "</thead>";
            try {
                int sucursal=0;
                while (con.rs.next()) {
                    String color = ((sucursal+1) % 2) == 0 ? "background-color: #e1e1e1;" : "";
                    this.msgtext += "<tr>";
                    this.msgtext += "<td style=\"border-bottom:1px solid #ccc;padding: 0px 5px;text-align: center;" + color + "\"><b>" + con.rs.getObject(1) + "</b></td>";
                    this.msgtext += "<td style=\"padding: 2px 4px; border-bottom:1px solid #ccc;padding: 0px 5px;text-align: center;" + color + "\">" + con.rs.getObject(3) + "</td>";
                    this.msgtext += "<td style=\"padding: 2px 4px; border-bottom:1px solid #ccc;padding: 0px 5px;text-align: center;" + color + "\">" + (new java.text.DecimalFormat("###,###,##0.00").format(con.rs.getDouble(4))) + "";
                    this.msgtext += "(" + (new java.text.DecimalFormat("###,###,##0.00").format(con.rs.getDouble(4) / 24.0)) + " d)</td>";
                    this.msgtext += "<td style=\"padding: 2px 4px; border-bottom:1px solid #ccc;padding: 0px 5px;text-align: center;" + color + "\">" + con.rs.getObject(6) + "</td>";
                    this.msgtext += "</tr>";
                    sucursal++;
                }

                System.out.println();
            } catch (SQLException e) {
            }
        }
        con.cerrarConexion();

        this.msgtext = this.msgtext + "</table></center> <br/>" + msgeneral;

        this.msgtext += "</html>";
    }

    /**
     * @param args se ignoran
     */
    public static void main(String[] args) {

        clssMail mail = new clssMail();

        mail.setInformacionMail();

        if (args.length > 0) {
            System.out.println("Fechas definidas.....");
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date date1 = sdf.parse(args[0]);
                Date date2 = sdf.parse(args[1]);
                mail.SetFechas(date1, date2);
            } catch (ParseException ex) {
                Logger.getLogger(clssMail.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
//        try {
//            Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse("2014-10-01");
//            Date date2 = new SimpleDateFormat("yyyy-MM-dd").parse("2014-10-31");
//            mail.SetFechas(date1, date2);
//        } catch (ParseException ex) {
//            Logger.getLogger(clssMail.class.getName()).log(Level.SEVERE, null, ex);
//        }                
        
        mail.generarreporte();
        mail.setParametros();
        if (mail.SendMail()) {
            System.out.println("Correo enviado, exitosamente");
        } else {
            System.out.println("No se pudo enviar el correo");
        }
    }
}
