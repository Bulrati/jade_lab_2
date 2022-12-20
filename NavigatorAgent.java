import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;;

public class NavigatorAgent extends Agent {
	private boolean goldIsFound = false;

    protected void setup() {
   
   	// Printout a welcome message
		System.out.println("Hello! Navigator-agent "+getAID().getName()+" is ready.");

		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("navigator-waiting");
		sd.setName("Navigating-through-caves");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}

		addBehaviour(new MessageHandlerServer());

	}

    protected void takeDown() {
    System.out.println("Navigator-agent "+getAID().getName()+" terminating.");
    }

	private class MessageHandlerServer extends CyclicBehaviour {

		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
			ACLMessage msg = myAgent.receive(mt);

			String finalMessage = "";

			if (msg != null) {
				String situation = msg.getContent();

				if(situation.contains("pit")) {
					finalMessage += "";
				} else if (situation.contains("breeze")) {
					finalMessage += "";
				} else if (situation.contains("snatch")) {
					finalMessage += "";
				} else if (situation.contains("wampus")) {
					finalMessage += "Go shoot him!";
				} else if (situation.contains("gold")) {
					finalMessage += "Try to grab it";
					goldIsFound = true;
				} else if (situation.contains("nothing")) {
					if (goldIsFound) {
						finalMessage += "Lets climb out of here";
					} else {
						finalMessage += "Go forward";
					}
				}
			} else {
				block();
			}
			
		}

	}

}