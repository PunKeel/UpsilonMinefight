package fr.PunKeel.Upsilon;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * User: PunKeel
 * Date: 9/28/13
 * Time: 5:27 PM
 * May be open-source & be sold (by PunKeel, of course !)
 */
class LogFormatter extends Formatter {
    private static final DateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");

    public String format(LogRecord record) {
        return df.format(new Date(record.getMillis())) + " - " + "[" + record.getSourceClassName() + "." + record.getSourceMethodName() + "] - " + "[" + record.getLevel() + "] - " + formatMessage(record) + "\n";
    }

    public String getHead(Handler h) {
        return super.getHead(h);
    }

    public String getTail(Handler h) {
        return super.getTail(h);
    }
}