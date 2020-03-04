package com.app.example.utils;

import java.io.File;
import java.io.FileWriter;

public class PowerUtil {
  public static void power(String id) {
    try {
      FileWriter localFileWriterOn = new FileWriter(new File("/proc/gpiocontrol/set_id"));
      localFileWriterOn.write(id);
      localFileWriterOn.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
