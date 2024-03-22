package fi.oulu.danielszabo.pepper.drs_services;

class Config {

  static final String DRS_HOST = "127.0.0.1";

  static final String SPEECH_SYNTH_URL_BASE = "http://" + DRS_HOST + ":59125/api/tts?voice=en_US/vctk_low#p236";
  static final String CA_URL_BASE = "http://" + DRS_HOST + ":5005/webhooks/rest/webhook";
  static final String SPEECH_REC_URL_BASE = "http://" + DRS_HOST + ":9001/api/v1/transcribe/?model=tiny.en.q5";
  static final String SPEECH_REC_API_KEY = "5e5ab95c454b43dc84be18a866680cffWih9FMxo8LoMREwuHScWT1QKVKwE9viv";

}
