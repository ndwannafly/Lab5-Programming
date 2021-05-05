package Commands.SpecificCommands;

import Commands.Command;
import Utils.Receiver;

import java.io.IOException;
import java.io.Serializable;

public class RemoveGreaterCommand extends Command implements Serializable {

    private Receiver receiver;
    private static final long serialVersionUID = 1234567L;

    public RemoveGreaterCommand(){

    }

    public RemoveGreaterCommand(Receiver receiver){
        this.receiver = receiver;
    }
    @Override
    public void aboutCommand() {
        System.out.println("remove_greater {element}    - remove all elements from the collection, which are greater than " +
                "specific element");
    }

    @Override
    public void execute(String[] args) throws IOException {
        if(args.length != 1){
            System.out.println("Client: Invalid command's format! Fail to execute RemoveGreaterCommand!");
        }
        else{
            receiver.removeGreater();
        }
    }
}
