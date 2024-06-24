package fi.oulu.danielszabo.pepper.interfaces;

import fi.oulu.danielszabo.pepper.services.drs.Instruction;

public interface InstructionHandler {
    boolean handleInstruction(Instruction instruction);
}
