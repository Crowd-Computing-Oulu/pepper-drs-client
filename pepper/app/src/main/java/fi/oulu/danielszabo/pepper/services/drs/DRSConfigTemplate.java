package fi.oulu.danielszabo.pepper.services.drs;


// Copy this file into DRSConfig.java and enter the missing values
class DRSConfigTemplate {

//  use this when running Java unit tests
//  static final String DRS_HOST = "127.0.0.1";
  // use this when running Android emulator
//  static final String DRS_HOST = "10.0.2.2";
  // use this when using Lenovo ThinkPad laptop as server
  static final String DRS_HOST = "1.2.3.4";

  static final String SPEECH_SYNTH_URL_BASE = "http://" + DRS_HOST + ":59125/api/tts?voice=en_US/vctk_low#p236";
  static final String CA_URL_BASE = "http://" + DRS_HOST + ":5005/webhooks/rest/webhook";
  static final String SPEECH_REC_URL_BASE = "http://" + DRS_HOST + ":9001/api/v1/transcribe/?model=tiny.en.q5";
  static final String SPEECH_REC_API_KEY = "yourkey";
  static final String CONTROL_HOST = "http://" + DRS_HOST + ":8080/index.php";

}
