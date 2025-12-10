package com.file.registry.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ApplicationConstants {

  public static final String XML_EXTENSION = ".xml";
  public static final String JSON_EXTENSION = ".json";
  public static final String FILE_NAME_SPLITTER = "_";
  public static final String CONTENT_DISPOSITION_TEMPLATE = "attachment; filename=\"%s\"";

  public class FilePartsConstants {
    public static final String CUSTOMER_INDEX_NAME = "index-by-customer";
    public static final String TYPE_INDEX_NAME = "index-by-type";
    public static final String DATE_INDEX_NAME = "index-by-date";

    public static final Integer CUSTOMER_INDEX_POSITION = 0;
    public static final Integer TYPE_INDEX_POSITION = 1;
    public static final Integer DATE_INDEX_POSITION = 2;
  }
}
