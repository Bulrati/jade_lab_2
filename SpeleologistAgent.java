import jade.core.Agent;

import java.util.Random;

import jade.core.AID;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;;

public class SpeleologistAgent extends Agent {
	private AID cave;
	private AID navigator;
	private MessageTemplate mt;
	private String[] pArray = {"It looks like a pit", "Here is a pit", "I see a pit"};
	private String[] bArray = {"I feel breeze here", "There is a breeze", "It`s a cool breeze here"};
	private String[] sArray = {"I feel snatch here", "There is a snatch", "It`s a snatch here"};
	private String[] wArray = {"I see a wampus", "A wampus is here", "Oh, it`s wampus"};
	private String[] gArray = {"I found gold", "A lot of gold", "It looks like gold"};
	private String[] nArray = {"I found nothing", "Its nothing here", "Pretty quiet, nothing"};
    
    protected void setup() {
   
   	// Printout a welcome message
		System.out.println("Hallo! Speleologist-agent "+getAID().getName()+" is ready.");

		addBehaviour(new FindCave());
		addBehaviour(new FindNavigator());
		
		addBehaviour(new TickerBehaviour(this, 60000) {
			protected void onTick() {
				addBehaviour(new SpeleologistLogic());
			}
		});
	}

    protected void takeDown() {
    System.out.println("Speleologist-agent "+getAID().getName()+" terminating.");
    }

	private class FindCave extends OneShotBehaviour {
		public void action()
		{
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("cave-walking");
			template.addServices(sd);
			try {
				DFAgentDescription[] result = DFService.search(myAgent, template); 
				for (int i = 0; i < result.length; ++i) {
					cave = result[i].getName();
				}
			}
			catch (FIPAException fe) {
				fe.printStackTrace();
			}
		}
	}

	private class FindNavigator extends OneShotBehaviour {
		public void action()
		{
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("navigator-waiting");
			template.addServices(sd);
			try {
				DFAgentDescription[] result = DFService.search(myAgent, template); 
				for (int i = 0; i < result.length; ++i) {
					navigator = result[i].getName();
				}
			}
			catch (FIPAException fe) {
				fe.printStackTrace();
			}
		}
	}

	private class SpeleologistLogic extends Behaviour {
		private int step = 0;
		
		public void action() {
			switch (step) {
				case 0:
				//Request to env
				ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
				req.addReceiver(cave);
				req.setConversationId("cave-exploring");
				req.setReplyWith("req"+System.currentTimeMillis()); // Unique value
				myAgent.send(req);
				// Prepare the template to get proposals
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("cave-exploring"),
						MessageTemplate.MatchInReplyTo(req.getReplyWith()));
				step = 1;
				break;
				case 1:
				//Get response from env and send it to navigator
				ACLMessage reply = myAgent.receive(mt);
				if (reply != null) {
					// Reply received
					if (reply.getPerformative() == ACLMessage.INFORM) {
						// This is an offer 
						String percept = reply.getContent();
						String[] percpets = percept.split(",");
						Random r = new Random();
						String finalString = "";

						for(int i=0; i<percpets.length;i++) {
							switch(percept) {
								case "P":
								finalString += pArray[r.nextInt(pArray.length)];
								break;
								case "B":
								finalString += bArray[r.nextInt(pArray.length)];
								break;
								case "S":
								finalString += sArray[r.nextInt(pArray.length)];
								break;
								case "W":
								finalString += wArray[r.nextInt(pArray.length)];
								break;
								case "G":
								finalString += gArray[r.nextInt(pArray.length)];
								break;
								default:
								finalString += nArray[r.nextInt(nArray.length)];
								break;
							}
						}
						ACLMessage toNav = new ACLMessage(ACLMessage.REQUEST);
						toNav.addReceiver(navigator);
						toNav.setConversationId("env-describing");
						toNav.setContent(finalString);
						toNav.setReplyWith("req"+System.currentTimeMillis()); // Unique value
						mt = MessageTemplate.and(MessageTemplate.MatchConversationId("env-describing"),
						MessageTemplate.MatchInReplyTo(toNav.getReplyWith()));
					}
					step = 2;
				}
				else {
					block();
				}
				break;
				case 2:
				//Get response from navigator and send it to env
				reply = myAgent.receive(mt);
				if (reply != null) {
					// Reply received
					if (reply.getPerformative() == ACLMessage.INFORM) {
					// This is an offer 
					String command = reply.getContent();
					String commandLiteral = "";
					if (command.contains("left")) {
						commandLiteral = "L";
					} else if (command.contains("right")) {
						commandLiteral = "R";
					} else if (command.contains("shoot")) {
						commandLiteral = "S";
					} else if (command.contains("forward")) {
						commandLiteral = "F";
					} else if (command.contains("climb")) {
						commandLiteral = "C";
					} else if (command.contains("grab")) {
						commandLiteral = "G";
					}
					ACLMessage toEnv = new ACLMessage(ACLMessage.REQUEST);
					toEnv.addReceiver(cave);
					toEnv.setConversationId("cave-action");
					toEnv.setContent(commandLiteral);
					toEnv.setReplyWith("req"+System.currentTimeMillis()); // Unique value
					mt = MessageTemplate.and(MessageTemplate.MatchConversationId("cave-action"),
					MessageTemplate.MatchInReplyTo(toEnv.getReplyWith()));
					step = 3;
				}
			}
				break;
				case 3:
				//Get response from env
				reply = myAgent.receive(mt);
				if (reply != null) {
					// Reply received
					if (reply.getPerformative() == ACLMessage.CONFIRM) {
						System.out.println("Gold found and rescued!");
						step = 4;
						doDelete();
					} else {
						step = 0;
					}
				break;
				}
			}
		}

		public boolean done() {
			return (step == 4);
		}
		
	}
}