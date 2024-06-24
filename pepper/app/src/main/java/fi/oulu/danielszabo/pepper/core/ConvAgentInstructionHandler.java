package fi.oulu.danielszabo.pepper.core;

import java.util.Timer;
import java.util.TimerTask;


import fi.oulu.danielszabo.pepper.PepperApplication;
import fi.oulu.danielszabo.pepper.interfaces.InstructionHandler;
import fi.oulu.danielszabo.pepper.log.LOG;
import fi.oulu.danielszabo.pepper.screens.main.MainFragment;
import fi.oulu.danielszabo.pepper.services.drs.Instruction;

public class ConvAgentInstructionHandler implements InstructionHandler {

    //    custom instruction from the CA server on top of the text response
    @Override
    public boolean handleInstruction(Instruction instruction) {
        LOG.debug(ConvAgentInstructionHandler.class, "Device instruction from CA: " + instruction.getIdentifier());
        switch (instruction.getIdentifier()) {
            case "transfer_to_wearable":
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        PepperApplication.mainActivity.runOnUiThread(() -> ((MainFragment) PepperApplication.mainActivity.fragment).transferToWearable());
                    }
                }, 3_000);
                PepperApplication.getControlService().sendMessage("transfer_conv_from");
                return true;
            default:
                LOG.error(ConvAgentInstructionHandler.class, "Unsupported device instruction in CA response: " + instruction.getIdentifier());
                return false;
        }
    }
}
