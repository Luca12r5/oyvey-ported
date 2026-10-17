package dev.lego.ui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;

/** Feeds Minecraft's log4j events into the in-game console (kept separate so a missing log4j-core only disables this). */
final class Log4jHook {
   private Log4jHook() {
   }

   static void attach() {
      LoggerContext ctx = (LoggerContext)LogManager.getContext(false);
      AbstractAppender appender = new AbstractAppender("LegoConsole", null, null, true, Property.EMPTY_ARRAY) {
         @Override
         public void append(LogEvent event) {
            String msg = event.getMessage() == null ? "" : event.getMessage().getFormattedMessage();
            if (event.getThrown() != null) {
               msg = msg + "\n" + event.getThrown();
            }

            String thread = event.getThreadName();
            ConsoleLog.add(event.getLevel().name(), thread == null ? "main" : thread, msg);
         }
      };
      appender.start();
      ctx.getConfiguration().getRootLogger().addAppender(appender, null, null);
      ctx.updateLoggers();
   }
}
