package org.example.utils;

public class GlobalValues {

    public static final String WODT_DT_BASE_LOCALHOST = "localhost";
    public static final String WODT_DT_BASE_DOCKER_HOST = "host.docker.internal";
    public static final String WODT_DT_BASE_HOST = WODT_DT_BASE_DOCKER_HOST;

    /* COMMON PROPERTIES KEYS */
    public final static String IDENTIFIER_KEY = "identifier";

    /* COMMON PARAMETERS */
    public final static String TYPE = "typeOfDt";
    public final static String PATIENT_TYPE = "patient";
    public final static String SURGERY_TYPE = "surgery";
    public final static String OPERATING_ROOM_TYPE = "operatingRoom";
    public final static String DEPARTMENT_TYPE = "department";
    public final static String VSM_TYPE = "vsm";
    public static final String REGISTER_PLATFORM = "http://localhost:8000";
    /* PATIENT */
    public static final String SURGERY_RELATIONSHIP_NAME = "surgery";
    public static final String SURGERY_RELATIONSHIP_TYPE = "surgery_rel";
    public static final String LOCATED_IN_RELATIONSHIP_NAME = "locatedIn";
    public static final String LOCATED_IN_RELATIONSHIP_TYPE = "locatedIn_rel";

    /* VSM */
    public static final String PATIENT_MONITORED_RELATIONSHIP_NAME = "patientMonitored";
    public static final String PATIENT_MONITORED_RELATIONSHIP_TYPE = "patientMonitored_rel";

    /* SURGERY */
    public static final String PATIENT_OPERATED_RELATIONSHIP_NAME = "patientOperated";
    public static final String PATIENT_OPERATED_RELATIONSHIP_TYPE = "patientOperated_rel";
    public static final String PROGRAMMED_IN_RELATIONSHIP_NAME = "programmedIn";
    public static final String EXECUTED_IN_RELATIONSHIP_NAME = "executedIn";
    public static final String PROGRAMMED_IN_RELATIONSHIP_TYPE = "programmedIn_rel";
    public static final String EXECUTED_IN_RELATIONSHIP_TYPE = "executedIn_rel";

    /* OPERATING ROOM */
    public static final String BELONGS_TO_NAME = "belongsTo";
    public static final String BELONGS_TO_TYPE = "belongsTo_rel";

    /* DEPARTMENT */
    public static final String SUPERVISE_SURGERY_NAME = "supervise";
    public static final String SUPERVISE_SURGERY_TYPE = "supervise_rel";
    public static final String OPERATING_ROOMS_NAME = "operatingRooms";
    public static final String OPERATING_ROOMS_TYPE = "operatingRooms_rel";

}
