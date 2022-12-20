import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class EnvironmentAgent extends Agent {
    private String[][] cave = new String[4][4];
	private int[] speleologistCoords = new int[2];
	private boolean goldIsColleted = false;

    protected void setup() {
   
   	// Printout a welcome message
		System.out.println("Hello! Environment-agent "+getAID().getName()+" is ready.");

		speleologistCoords[0] = 0;
		speleologistCoords[1] = 0;

		cave[0][0] = "";
		cave[0][1] = "B";
		cave[0][2] = "P";
		cave[0][3] = "B";
		cave[1][0] = "S";
		cave[1][1] = "";
		cave[1][2] = "B";
		cave[1][3] = "";
		cave[2][0] = "W";
		cave[2][1] = "B,S,G";
		cave[2][2] = "P";
		cave[2][3] = "B";
		cave[3][0] = "S";
		cave[3][1] = "";
		cave[3][2] = "B";
		cave[3][3] = "P";

		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("cave-walking");
		sd.setName("Cave-with-treasure-walk");
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
		try {
			DFService.deregister(this);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		System.out.println("Environment-agent "+getAID().getName()+" terminating.");
    }

    private class MessageHandlerServer extends CyclicBehaviour {

		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
			ACLMessage msg = myAgent.receive(mt);

			if (msg != null) {
				String descriptionOfCurrentPostiton = cave[speleologistCoords[0]][speleologistCoords[1]];
				ACLMessage reply = msg.createReply();
				reply.setPerformative(ACLMessage.INFORM);
				reply.setContent(descriptionOfCurrentPostiton);
				myAgent.send(reply);
			} else {
				MessageTemplate mt2 = MessageTemplate.MatchPerformative(ACLMessage.CFP);
				ACLMessage msg2 = myAgent.receive(mt2);

				if (msg2 != null) {
					String action = msg2.getContent();

					switch(action) {
						case "R":
							if (speleologistCoords[1] < 3) {
								speleologistCoords[1]++;
							}
							ACLMessage reply2 = msg2.createReply();
					reply2.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
					reply2.setContent("OK");
					myAgent.send(reply2);
						break;
						case "L":
						if (speleologistCoords[1] > 1) {
							speleologistCoords[1]--;
						}
						 reply2 = msg2.createReply();
					reply2.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
					reply2.setContent("OK");
					myAgent.send(reply2);
						break;
						case "F":
						if (speleologistCoords[0] < 3) {
							speleologistCoords[0]++;
						}
						 reply2 = msg2.createReply();
					reply2.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
					reply2.setContent("OK");
					myAgent.send(reply2);
						break;
						case "S":
							if (cave[speleologistCoords[0]][speleologistCoords[1]] == "W")
							{
								System.out.println("Wampus is dead");
								cave[speleologistCoords[0]][speleologistCoords[1]] = "";
							}
							 reply2 = msg2.createReply();
					reply2.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
					reply2.setContent("OK");
					myAgent.send(reply2);
						break;
						case "G":
							if (cave[speleologistCoords[0]][speleologistCoords[1]] == "G")
							{
								System.out.println("Gold is collected");
								goldIsColleted = true;
							}
							 reply2 = msg2.createReply();
					reply2.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
					reply2.setContent("OK");
					myAgent.send(reply2);
						break;
						case "C":
						 reply2 = msg2.createReply();
						 if (goldIsColleted) {
							reply2.setPerformative(ACLMessage.CONFIRM);
						 } else {
							reply2.setPerformative(ACLMessage.REFUSE);
						 }
						
						reply2.setContent("OK");
						myAgent.send(reply2);
						break;
						default:
						break;
					}
				} else {
					block();
				}
			}
		}

	}
}