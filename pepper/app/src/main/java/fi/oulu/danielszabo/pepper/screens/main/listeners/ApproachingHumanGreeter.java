package fi.oulu.danielszabo.pepper.screens.main.listeners;

import com.aldebaran.qi.sdk.object.human.Human;
import com.aldebaran.qi.sdk.object.humanawareness.HumanAwareness;

import java.util.ArrayList;
import java.util.List;

import fi.oulu.danielszabo.pepper.tools.SimpleController;

public class ApproachingHumanGreeter extends CustomPepperEventListener implements HumanAwareness.OnHumansAroundChangedListener {

//    last time we checked, these humans were around. Initially no one
    private List<Human> lastHumansAroundList = new ArrayList<>();

    @Override
    public void onHumansAroundChanged(List<Human> humans) {

        runWithTimeout(() -> {
//            first person arrives to pepper
            if(lastHumansAroundList.size() == 0 && humans.size() == 1){
                SimpleController.say("Hi!");
//            one more person joins
            } else if(lastHumansAroundList.size() > 0 && humans.size() > lastHumansAroundList.size()){
                SimpleController.say("Hi!");
            }
        }, 45_000);

        lastHumansAroundList = humans;
    }

}
