package io.github.agentsoz.ees;

/*-
 * #%L
 * Emergency Evacuation Simulator
 * %%
 * Copyright (C) 2014 - 2021 by its authors. See AUTHORS file.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */


import io.github.agentsoz.dataInterface.DataClient;
import io.github.agentsoz.dataInterface.DataServer;
import io.github.agentsoz.dataInterface.DataSource;
import io.github.agentsoz.socialnetwork.ICModel;
import io.github.agentsoz.socialnetwork.SNConfig;
import io.github.agentsoz.socialnetwork.SocialNetworkDiffusionModel;
import io.github.agentsoz.util.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class DiffusionModel implements DataSource<SortedMap<Double, DiffusionDataContainer>>, DataClient<Object> {

    private final Logger logger = LoggerFactory.getLogger(DiffusionModel.class);

    private static final String eConfigFile = "configFile";

    private DataServer dataServer;
    private double startTimeInSeconds = -1;
    private SocialNetworkDiffusionModel snManager;
    private double lastUpdateTimeInMinutes = -1;
    private Time.TimestepUnit timestepUnit = Time.TimestepUnit.SECONDS;
    private String configFile = null;
    private List<String> agentsIds = null;

    private TreeMap<Double,DiffusionDataContainer> allStepsDiffusionData;

    Map<String, Set> localContentFromAgents;
    ArrayList<String> globalContentFromAgents;

    public DiffusionModel(String configFile) {
        this.snManager = (configFile==null) ? null : new SocialNetworkDiffusionModel(configFile);
        this.localContentFromAgents = new HashMap<>();
        this.globalContentFromAgents =  new ArrayList<String>();
        this.allStepsDiffusionData =  new TreeMap<>();
    }

    public DiffusionModel(Map<String, String> opts, DataServer dataServer, List<String> agentsIds) {
        parse(opts);
        this.snManager = (configFile==null) ? null : new SocialNetworkDiffusionModel(configFile,dataServer);
        this.localContentFromAgents = new HashMap<>();
        this.globalContentFromAgents =  new ArrayList<String>();
        this.allStepsDiffusionData =  new TreeMap<>();
        this.dataServer = dataServer;
        this.agentsIds = agentsIds;
    }

    private void parse(Map<String, String> opts) {
        if (opts == null) {
            return;
        }
        for (String opt : opts.keySet()) {
            logger.info("Found option: {}={}", opt, opts.get(opt));
            switch(opt) {
                case Config.eGlobalStartHhMm:
                    String[] tokens = opts.get(opt).split(":");
                    setStartHHMM(new int[]{Integer.parseInt(tokens[0]),Integer.parseInt(tokens[1])});
                    break;
                case eConfigFile:
                    configFile = opts.get(opt);
                    break;
                default:
                    logger.warn("Ignoring option: " + opt + "=" + opts.get(opt));
            }
        }
    }

    public void setStartHHMM(int[] hhmm) {
        startTimeInSeconds = Time.convertTime(hhmm[0], Time.TimestepUnit.HOURS, timestepUnit)
                + Time.convertTime(hhmm[1], Time.TimestepUnit.MINUTES, timestepUnit);
    }


    public void init(List<String> idList) {

        this.snManager.setupSNConfigsAndLogs(); // first, setup configs and create log
        for (String id : idList) {
            this.snManager.createSocialAgent(id); //populate agentmap
        }
        this.snManager.genNetworkAndDiffModels(); // setup configs, gen network and diffusion models
        this.snManager.printSNModelconfigs();
        //subscribe to BDI data updates
      //  this.dataServer.subscribe(this, Constants.DIFFUSION_UPDATES_FROM_BDI_AGENT);
        this.dataServer.subscribe(this, Constants.DIFFUSION_DATA_CONTAINDER_FROM_BDI);
    }

    protected void stepDiffusionProcess(DiffusionDataContainer dataContainer, String contentType, double timestep) {
        snManager.stepDiffusionModels(timestep); // step the diffusion model

        if (snManager.getDiffModels()[0] instanceof ICModel) {
            ICModel icModel = (ICModel) snManager.getDiffModels()[0];
            icModel.recordCurrentStepSpread((int)timestep);

            HashMap<String, ArrayList<String>> latestUpdate = icModel.getLatestDiffusionUpdates();
            if (!latestUpdate.isEmpty()) {

                for(Map.Entry<String,ArrayList<String>> contents: latestUpdate.entrySet()) {
                    String content = contents.getKey();
                    ArrayList<String> agentIDs = contents.getValue();
                    logger.info("agents activated for content {} at time {} are: {}",content,(int)timestep,agentIDs.toString());

                    for(String id: agentIDs) { // for each agent create a DiffusionContent and put content type and parameters
                     //   DiffusionContent content = dataContainer.getOrCreateDiffusedContent(id);
                        String[] params = {content};
                        dataContainer.putContentToContentsMapFromDiffusionModel(id,contentType, params);
                      //  content.getContentsMapFromDiffusionModel().put(contentType,params );
                    }
                }

                }

        }


    }


    @Override
    public SortedMap<Double, DiffusionDataContainer> sendData(double timestep, String dataType) {

        double currentTimeInMinutes = Time.convertTime(timestep, timestepUnit, Time.TimestepUnit.MINUTES); // current time in minutes
        Double nextTime = timestep + snManager.getEarliestTimeForNextStep();

        // create data structure to store current step contents and params
        DiffusionDataContainer currentStepDataContainer =  new DiffusionDataContainer();


        if (nextTime != null) {
            dataServer.registerTimedUpdate(Constants.DIFFUSION_DATA_CONTAINER_FROM_DIFFUSION_MODEL, this, nextTime);
            // update the model with any new messages form agents
            ICModel icModel = (ICModel) this.snManager.getDiffModels()[0]; //#FIXME hardcoded to use first model, as ICModel

            if (!localContentFromAgents.isEmpty()) { // update local content
                Map<String, String[]> map = new HashMap<>();
                for (String key : localContentFromAgents.keySet()) {
                    Object[] set = localContentFromAgents.get(key).toArray(new String[0]);
                    String[] newSet = new String[set.length];
                    for (int i = 0; i < set.length; i++) {
                        newSet[i] = (String)set[i];
                    }
                    map.put(key,newSet);
                    logger.info(String.format("At time %.0f, total %d agents received content %s from BDI Model.", timestep, newSet.length, key));
                    logger.info("Agents spreading content are: {}", Arrays.toString(newSet));
                }
                icModel.updateSocialStatesFromLocalContent(map);
            }

            if(!globalContentFromAgents.isEmpty()) { // update global contents

                logger.info("Global content received to spread: {}", globalContentFromAgents.toString());
                icModel.updateSocialStatesFromGlobalContent(globalContentFromAgents);

            }

            // step the models
            stepDiffusionProcess(currentStepDataContainer,Constants.EVACUATION_INFLUENCE,currentTimeInMinutes);

            //now put the current step data container to all steps data map
           if(!currentStepDataContainer.getDiffusionDataMap().isEmpty()){
               this.allStepsDiffusionData.put(currentTimeInMinutes, currentStepDataContainer);
           }



            // clear the contents
            globalContentFromAgents.clear();
            localContentFromAgents.clear();

        }

        //+1 to avoid returning empty map for diffusion data for first step (toKey = fromKey)
        SortedMap<Double, DiffusionDataContainer> periodicDiffusionData =   allStepsDiffusionData.subMap(lastUpdateTimeInMinutes,currentTimeInMinutes+1);
        lastUpdateTimeInMinutes = currentTimeInMinutes;

        return (currentStepDataContainer.getDiffusionDataMap().isEmpty()) ? null : periodicDiffusionData;

    }


    @Override
    public void receiveData(double time, String dataType, Object data) { // data package from the BDI side

        switch (dataType) {
            case Constants.DIFFUSION_DATA_CONTAINDER_FROM_BDI: // update Diffusion model based on BDI updates

                DiffusionDataContainer dataContainer = (DiffusionDataContainer) data;

                if (!(data instanceof DiffusionDataContainer)) {
                    logger.error("received unknown data: " + data.toString());
                    break;
                }

                    for (Map.Entry entry : dataContainer.getDiffusionDataMap().entrySet()) {

                    String agentId = (String) entry.getKey();
                    DiffusionContent dc = (DiffusionContent) entry.getValue();

                        //process local contents from the BDI model
                        if(!dc.getContentsMapFromBDIModel().isEmpty()){
                            for(String localContent: dc.getContentsMapFromBDIModel().keySet()){
                                String[] contents = (String[]) dc.getContentsMapFromBDIModel().get(localContent);
                                String content = contents[0];
                                // do something with parameters

                                logger.debug("Agent {} received content {} of type {} ",agentId,content,localContent);
                                Set<String> agents = (localContentFromAgents.containsKey(content)) ? localContentFromAgents.get(content) :
                                        new HashSet<>();
                                agents.add(agentId);
                                localContentFromAgents.put(content, agents);
                            }
                        }


                        //process global (broadcast) contents from BDI model
                        if(!dc.getBroadcastContentsMapFromBDIModel().isEmpty()){
                            for(String globalContent: dc.getBroadcastContentsMapFromBDIModel().keySet()){
                                logger.debug("received global content " + globalContent);
                                if(!globalContentFromAgents.contains(globalContent)) {
                                    globalContentFromAgents.add(globalContent);
                                }
                                String[] params = (String[])dc.getBroadcastContentsMapFromBDIModel().get(globalContent);
                                // do something with parameters

                            }
                        }


                        //process SN actions
                        if(!dc.getSnActionsMapFromBDIModel().isEmpty()){
                            for(String action: dc.getSnActionsMapFromBDIModel().keySet()){
                                Object[] params = dc.getSnActionsMapFromBDIModel().get(action);
                                // do something with parameters
                            }
                        }
                    }

                break;
            default:
                throw new RuntimeException("Unknown data type received: " + dataType);
        }
    }



    /**
     * Set the time step unit for this model
     * @param unit the time step unit to use
     */
    void setTimestepUnit(Time.TimestepUnit unit) {
        timestepUnit = unit;
    }


    public void start() {
        if (snManager != null) {
            init(agentsIds);
            setTimestepUnit(Time.TimestepUnit.MINUTES);
            dataServer.registerTimedUpdate(Constants.DIFFUSION_DATA_CONTAINER_FROM_DIFFUSION_MODEL, this, Time.convertTime(startTimeInSeconds, Time.TimestepUnit.SECONDS, timestepUnit));
        } else {
            logger.warn("started but will be idle forever!!");
        }
    }

    /**
     * Start publishing data
     * @param hhmm an array of size 2 with hour and minutes representing start time
     */
    public void start(int[] hhmm) {
        double startTimeInSeconds = Time.convertTime(hhmm[0], Time.TimestepUnit.HOURS, Time.TimestepUnit.SECONDS)
                + Time.convertTime(hhmm[1], Time.TimestepUnit.MINUTES, Time.TimestepUnit.SECONDS);
        dataServer.registerTimedUpdate(Constants.DIFFUSION_DATA_CONTAINER_FROM_DIFFUSION_MODEL, this, startTimeInSeconds);
    }


    public void finish() {
        // cleaning

        if(snManager == null) { // return if the diffusion model is not executed
            return;
        }
        if (snManager.getDiffModels()[0] instanceof ICModel) {

            //terminate diffusion model and output diffusion data
            ICModel icModel = (ICModel) this.snManager.getDiffModels()[0];
            icModel.finish();
            icModel.getDataCollector().writeSpreadDataToFile();
        }
    }

    /**
     * Sets the publish/subscribe data server
     * @param dataServer the server to use
     */
    void setDataServer(DataServer dataServer) {
        this.dataServer = dataServer;
    }

    public DataServer getDataServer() {
        return dataServer;
    }


    public SocialNetworkDiffusionModel getSnManager() {
        return snManager;
    }


    public TreeMap<Double, DiffusionDataContainer> getAllStepsDiffusionData() {
        return allStepsDiffusionData;
    }

    public Map<String, Set> getLocalContentFromAgents() {
        return localContentFromAgents;
    }

    public ArrayList<String> getGlobalContentFromAgents() {
        return globalContentFromAgents;
    }
}
