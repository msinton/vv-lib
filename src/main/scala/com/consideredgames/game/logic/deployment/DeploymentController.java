package com.consideredgames.game.logic.deployment;//package com.consideredGames.client.model.logic.phase.deployment;
//
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//import java.util.ListIterator;
//import java.util.Set;
//
//import javax.inject.Inject;
//
//import com.consideredGames.client.model.movement.MovementMapManager;
//import com.consideredGames.client.model.movement.MovementMap;
//import com.consideredGames.client.model.person.NewPersonInstructions;
//import org.springframework.stereotype.Component;
//
//import com.consideredGames.client.events.MessageType;
//import com.consideredGames.client.model.board.BoardData;
//import com.consideredGames.client.model.hex.Hex;
//import com.consideredGames.client.model.logic.phase.PlacementHelper;
//import com.consideredGames.client.model.person.Person;
//import com.consideredGames.client.model.player.Player;
//
//@Component
//public class DeploymentController {
//
//    private final BoardData boardData;
//
//    private final MovementMapManager movementMapManager;
//
//    private final PlacementHelper placementHelper;
//
//    private List<Set<Hex>> availableHexGroups = new ArrayList<>();
//
//    private int initialPeopleNum = 6;
//
//    private List<Person> peopleToDeploy = new ArrayList<>();
//
//    private int peoplePlaced = 0;
//
//    private Set<Hex> hexGroupChosen = null;
//
//    private List<DeploymentAction> deployments = new ArrayList<>();
//
//    @Inject
//    public DeploymentController(BoardData boardData, MovementMapManager movementMapManager,
//            PlacementHelper placementHelper) {
//        this.boardData = boardData;
//        this.movementMapManager = movementMapManager;
//        this.placementHelper = placementHelper;
//    }
//
//    /**
//     * Clears all state information, for object re-use.
//     */
//    public void clear() {
//        availableHexGroups.clear();
//        peopleToDeploy.clear();
//        peoplePlaced = 0;
//        hexGroupChosen = null;
//        deployments.clear();
//    }
//
//    public List<Person> getPeopleToDeploy() {
//        return peopleToDeploy;
//    }
//
//    public void initPeopleToDeploy(Player player, int number) {
//        for (int i = 0; i < initialPeopleNum; i++) {
//            peopleToDeploy.add(player.create(new NewPersonInstructions(player.colour(), scala.Option.apply((null)), 1)));
//        }
//    }
//
//    /**
//     * Intended to be called at the start of the game to produce the required people and define where they may be deployed.
//     */
//    public void initialDeploySetup(Player player) {
//        // set which hexes are allowed.
//
//        initPeopleToDeploy(player, initialPeopleNum);
//
//        for (Set<Hex> hexGroup : boardData.getRiverNetwork().getGroups()) {
//            boolean groupEmpty = true;
//            for (Hex hex : hexGroup) {
//                if (hex.person().isDefined()) {
//                    groupEmpty = false;
//                    break;
//                }
//            }
//            if (groupEmpty) {
//                availableHexGroups.add(hexGroup);
//            }
//        }
//    }
//
//    /**
//     * Intended to be used on the initial person placement only.
//     * @return
//     */
//    public Set<Hex> getInitialPossiblePositions() {
//
//        if (hexGroupChosen == null) {
//            Set<Hex> hexes = new HashSet<>();
//            for (Set<Hex> hexGroup : availableHexGroups) {
//                hexes.addAll(hexGroup);
//                return hexes;
//            }
//        }
//        return hexGroupChosen;
//    }
//
//    public boolean placeUnit(Player player, Person person, Hex hex) {
//
//        if (isOkForPlacement(person, hex)) {
//            doPlacement(player, person, hex);
//            return true;
//        }
//        return false;
//    }
//
//    public boolean tryDoPlacements(Player player, List<DeploymentAction> placements) {
//
//        ListIterator<DeploymentAction> itr = placements.listIterator();
//
//        boolean fail = false;
//
//        while (!fail && itr.hasNext()) {
//            DeploymentAction placement = itr.next();
//            if (!placeUnit(player, player.people().get(placement.getPerson()).get(), boardData.getHex(placement.getHex()))) {
//                fail = true;
//            }
//        }
//
//        if (fail) {
//            while (itr.hasPrevious()) {
//                DeploymentAction placement = itr.previous();
//                undoPlaceUnit(player, player.people().get((placement.getPerson())).get(), boardData.getHex(placement.getHex()));
//            }
//        }
//
//        return !fail;
//    }
//
//    public boolean undoPlaceUnit(Player player, Person person, Hex hex) {
//        for (DeploymentAction action : deployments) {
//            if (action.getPerson() == person.id() && action.getHex() == hex.id()) {
//                deployments.remove(action);
//                peopleToDeploy.add(person);
//                peoplePlaced--;
//
//                player.kill(person.id());
//                person.hex_$eq(scala.Option.apply(null));
//
//                if (peoplePlaced == 0) {
//                    hexGroupChosen = null;
//                }
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public Message submit() {
//        Message message = new Message();
//        message.setMessageType(MessageType.DEPLOYMENT);
//        DeploymentEvent deploymentEvent = new DeploymentEvent();
//        deploymentEvent.setPlacements(deployments);
//        message.setDeploymentEvent(deploymentEvent);
//        return message;
//    }
//
//    private boolean isOkForPlacement(Person person, Hex hex) {
//        boolean hexOk = false;
//        if (peoplePlaced == 0) {
//            for (Set<Hex> hexes : availableHexGroups) {
//                if (hexes.contains(hex)) {
//                    this.hexGroupChosen = hexes;
//                    hexOk = true;
//                    break;
//                }
//            }
//        } else {
//            // Check was viable hex - adjacent to another person
//            //TODO - after fixed movementMap stuff
////            MovementMap movementMap = movementMapManager.getMap(hex, person);
////            if (movementMap.getMap(1).get(1).containsKey(hex)) {
////                hexOk = true;
////            }
//        }
//        return hexOk;
//    }
//
//    private void doPlacement(Player player, Person person, Hex hex) {
//        player.people().put(person.id(), person);
//
//        DeploymentAction action = placementHelper.place(person, hex);
//        deployments.add(action);
//        peopleToDeploy.remove(person);
//
//        peoplePlaced++;
//    }
//
//    // New people to deploy
//    // people may be imbued with skills
//
//    //The first time deployment is done there are 6 new people.
//
//    //Placement is restricted to an "empty" sector - with no other people. The map is divided into sectors by the river.
//
//    // The
//
//    //TODO no hexes available but still have people to deploy? - message to player
//}
