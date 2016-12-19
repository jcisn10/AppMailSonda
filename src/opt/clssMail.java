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
 * Objetivo: La funcionalidad de esta aplicación es la de enviar correos,
 * haciendo la función de enviar reportes y enviar alertas de advertencias a los
 * integrantes de la lista de grupo.
 *
 * @author jorge.cisneros email: jorge.cisneros@sonda.com /
 * jcisneros.cisneros250@gmail.com
 */
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class clssMail {

    String smtp = "", puerto = "", usuario = "", clave = "";
    String seguridad = "", autenticacion = "", shares = "";
    String asunto = "", mensaje = "", msgtext = "", para = "", cco = "", bcc = "";
    Properties prt = new Properties();
    java.util.Date fechaini = null, fechafin = null;
    static String SQL = "", SERVER = "", DBSERVER = "", CLAVEDB = "", USER = "";
    String SUCURSALES = "";
    SimpleDateFormat parseFecha = new SimpleDateFormat("yyyy-MM-dd");
    Locale locale = new Locale("es", "PA");
    String patronNumerico = "###,###,##0.00";
    DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getNumberInstance(locale);
    int alerta1 = 0, alerta2 = 0, alerta3 = 0, operacion = 0;

    //TODO: Mejorar envio de mensajes con adjuntos
    public clssMail() {
        decimalFormat.applyPattern(patronNumerico);
        FileInputStream finpt = null;
        try {
            finpt = new java.io.FileInputStream("configmail.properties");
            prt.load(finpt);
            smtp = prt.getProperty("SMTP");
            puerto = prt.getProperty("PUERTO");
            seguridad = prt.getProperty("SEGURIDADMAIL", "true");
            usuario = prt.getProperty("USUARIO");
            autenticacion = prt.getProperty("AUTENTICACION", "true");
            clave = prt.getProperty("CLAVE");
            operacion = Integer.parseInt(prt.getProperty("ACCION"));
            /**
             * ****PARAMETROS DE CONEXION DE DB SQL****
             */
            CLAVEDB = prt.getProperty("CLAVEDB");
            SERVER = prt.getProperty("SERVER");
            DBSERVER = prt.getProperty("DBSQLSERVER");
            USER = prt.getProperty("USER");
            alerta1 = Integer.parseInt(prt.getProperty("ALERTA1"));
            alerta2 = Integer.parseInt(prt.getProperty("ALERTA2"));
            alerta3 = Integer.parseInt(prt.getProperty("ALERTA3"));
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    /**
     * Función de retornar valor de direccion del servidor
     *
     * @return
     */
    public static String getSERVER() {
        return SERVER;
    }

    /**
     * Función de retornar valor de nombre de la base de datos
     *
     * @return
     */
    public static String getDBSERVER() {
        return DBSERVER;
    }

    /**
     * Función de retornar valor de usuario
     *
     * @return
     */
    public static String getUSUARIO() {
        return USER;
    }

    /**
     * Función de retornar valor de la clave
     *
     * @return
     */
    public static String getCLAVEDB() {
        return CLAVEDB;
    }

    /**
     * Función que establece los parametros
     */
    public void setParametros() {
        prt.put("mail.smtp.host", smtp);
        prt.setProperty("mail.smtp.ssl.trust", smtp);
        prt.setProperty("mail.smtp.starttls.enable", seguridad);
        prt.setProperty("mail.smtp.port", puerto);
        prt.setProperty("mail.smtp.user", usuario);
        prt.setProperty("mail.smtp.auth", autenticacion);
    }

    /**
     * Función para setear las fechas de inicio y fin
     *
     * @param inicio
     * @param fin
     */
    public void SetFechas(Date inicio, Date fin) {
        this.fechaini = inicio;
        this.fechafin = fin;
    }

    /**
     * Función que obtiene estatus de url (ping)
     *
     * @param url
     * @return
     * @throws IOException
     */
    public static boolean getStatus(String url) throws IOException {
        boolean result = false;
        try {
            URL siteURL = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) siteURL.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int code = connection.getResponseCode();
            if (code == 200) {
                result = true;
            }
        } catch (IOException e) {
            result = false;
        }
        return result;
    }

    public static boolean ping(String ip) {
        boolean sw = false;
        try {
            InetAddress[] addresses = InetAddress.getAllByName(ip);
            for (InetAddress addr : addresses) {
                sw = addr.isReachable(2000);
            }
        } catch (IOException e) {
            sw = false;
            System.out.println("host is unknown (or unresolvable)");
        }
        return sw;
    }

    /**
     * Función que setea los valores para el envío de correos.
     */
    public void setInformacionMail() {
        shares = prt.getProperty("ADJUNTOS");
        this.para = prt.getProperty("PARA");
        this.cco = prt.getProperty("CCO");
        this.bcc = prt.getProperty("BCC");

        this.asunto = "Saludos"; //prt.getProperty("ASUNTO");
        this.mensaje = "Mensaje de Prueba"; //prt.getProperty("MENSAJE");
        //this.adjuntos = shares.split(";");
    }

    public void setAsuntoMsg(String a, String b) {
        this.asunto = a;
        this.mensaje = b;
    }

    /**
     * Función que envía el correo con los valores indicados para el envío de
     * los avisos y reportes.
     *
     * @return
     */
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
            this.msgtext = this.mensaje;
            texto.setContent(this.msgtext, "text/html");

            String fichero = "";
//            String linea;

            // Una MultiParte para agrupar texto e imagen.
            MimeMultipart multiParte = new MimeMultipart();

            multiParte.addBodyPart(texto);

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

            //javax.activation.DataSource datasrc = null;
            //BodyPart firma = new MimeBodyPart();
            //firma.setContent(fichero, "text/html");
            //multiParte.addBodyPart(firma);
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

            message.setSubject(this.asunto);
            message.setContent(multiParte);

            // Se envia el correo.
            Transport t = session.getTransport("smtp");
            t.connect(null, usuario, clave);
            t.sendMessage(message, message.getAllRecipients());
            t.close();
            status = true;
        } catch (MessagingException e) {
            e.printStackTrace();
            status = false;
        }
        return status;
    }

    /**
     * @param args se ignoran
     */
    public static void main(String[] args) {
        clssMssql connServer = new clssMssql();
        @SuppressWarnings("UnusedAssignment")
        String url = "", Asunto = "", Msg = "";
        String tablaMonitor = "";
        String tablaTasStackerFull = "";
        int horamail = new java.util.Date().getHours();
        String saludoIni = (horamail < 13) ? "Buenos días, " : (horamail >= 13 && horamail < 18) ? "Buenas tardes, " : "Buenas noches, ";

        clssMail mail = new clssMail();
        url = "jdbc:sqlserver://"
                + clssMail.getSERVER() + ":1433;databaseName="
                + clssMail.getDBSERVER() + ";integratedSecurity=false;";

        connServer.dbConnect(url, "com.microsoft.sqlserver.jdbc.SQLServerDriver",
                clssMail.getUSUARIO(),
                clssMail.getCLAVEDB());

        tablaMonitor = connServer.qryMonitorTaSondaMail();
        tablaTasStackerFull = connServer.qryMonitorTaSonda();

        Asunto = "Mensaje de Advertencia en Tas " + connServer.getPosId();
        Msg += saludoIni;
        Msg += " Favor retirar el Stacker que contiene arriba de " + mail.alerta2 + " cantidad de billetes. " + "<hr>";

        if (args.length > 0) {
            int var = Integer.parseInt(args[0]);
            if (var == 1) {
                Msg += "<br/>" + tablaTasStackerFull + "<br/>";
            } else if (var == 2) {
                Msg += "<br/>" + tablaMonitor + "<br/>";
            } else if (var == 3) {
                Msg += "<br/>" + "---------------->Staker desconocido " + (new java.text.SimpleDateFormat("DD/MM/YYYY").format(new java.util.Date().getDate())) + "<----------------" + "<br/>";
            }
        } else {
            if (mail.operacion == 1) {
                Msg += "<br/>" + tablaTasStackerFull + "<br/>";
            } else if (mail.operacion == 2) {
                Msg += "<br/>" + tablaMonitor + "<br/>";
            } else if (mail.operacion == 3) {
                Msg += "<br/>" + "---------------->Staker desconocido " + (new java.text.SimpleDateFormat("DD/MM/YYYY").format(new java.util.Date().getDate())) + "<----------------" + "<br/>";
            }
        }

        mail.setInformacionMail();
//        if (connServer.getCantStacker() > mail.alerta2) {
//        }        

        if (args.length > 0) {
            String asunto = args[0];
            String mensaje = args[1];
            mail.setAsuntoMsg(asunto, mensaje);
        }

        mail.setAsuntoMsg(Asunto, Msg + "<hr>");
        mail.setParametros();
        if (mail.SendMail()) {
            System.out.println("Correo enviado, exitosamente");
        } else {
            System.out.println("No se pudo enviar el correo");
        }
    }
}
