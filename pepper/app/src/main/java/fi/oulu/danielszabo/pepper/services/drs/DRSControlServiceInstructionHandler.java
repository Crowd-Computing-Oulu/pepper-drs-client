package fi.oulu.danielszabo.pepper.services.drs;

import fi.oulu.danielszabo.pepper.PepperApplication;
import fi.oulu.danielszabo.pepper.interfaces.InstructionHandler;
import fi.oulu.danielszabo.pepper.log.LOG;
import fi.oulu.danielszabo.pepper.screens.main.MainFragment;

public class DRSControlServiceInstructionHandler implements InstructionHandler {

    @Override
    public boolean handleInstruction(Instruction instruction) {
        LOG.debug(DRSControlServiceInstructionHandler.class, "Device instruction from CA: " + instruction.getIdentifier());
        switch (instruction.getIdentifier()) {
            case "":
                return true;
            default:
                LOG.error(DRSControlServiceInstructionHandler.class, "Unsupported device instruction in CA response: " + instruction.getIdentifier());
                return false;
        }
    };
}
