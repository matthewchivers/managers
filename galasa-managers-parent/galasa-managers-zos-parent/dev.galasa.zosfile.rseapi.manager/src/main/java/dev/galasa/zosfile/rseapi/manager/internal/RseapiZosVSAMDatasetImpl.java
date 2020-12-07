/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosfile.rseapi.manager.internal;

import java.nio.file.Path;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.galasa.ResultArchiveStoreContentType;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zosfile.IZosDataset;
import dev.galasa.zosfile.IZosDataset.DatasetDataType;
import dev.galasa.zosfile.IZosDataset.DatasetOrganization;
import dev.galasa.zosfile.IZosDataset.RecordFormat;
import dev.galasa.zosfile.IZosDataset.SpaceUnit;
import dev.galasa.zosfile.IZosVSAMDataset;
import dev.galasa.zosfile.ZosDatasetException;
import dev.galasa.zosfile.ZosFileManagerException;
import dev.galasa.zosfile.ZosVSAMDatasetException;
import dev.galasa.zosrseapi.IRseapi.RseapiRequestType;
import dev.galasa.zosrseapi.IRseapiResponse;
import dev.galasa.zosrseapi.IRseapiRestApiProcessor;
import dev.galasa.zosrseapi.RseapiException;

public class RseapiZosVSAMDatasetImpl implements IZosVSAMDataset {
    
    private IRseapiRestApiProcessor rseapiApiProcessor;

	private RseapiZosFileHandlerImpl zosFileHandler;
	public RseapiZosFileHandlerImpl getzosFileHandler() {
		return zosFileHandler;
	}

    private RseapiZosDatasetImpl zosDataset;

    private IZosImage image;
    
    private static final String SLASH = "/";
    private static final String RESTUNIXCOMMANDS_PATH = SLASH + "rseapi" + SLASH + "api" + SLASH + "v1" + SLASH + "unixcommands";

    // Required parameters
    private String name;
    private VSAMSpaceUnit spaceUnit;
    private int primaryExtents;
    private int secondaryExtents;
    private String volumes;
    private boolean retainToTestEnd = false;
    private boolean datasetCreated = false;

    // Optional parameters
    private String accountInfo;
    private long bufferspace;
    private BWOOption bwoOption;
    private String controlInterval;
    private String dataclass;
    private EraseOption eraseOption;
    private String exceptionExit;
    private Integer freeSpaceControlInterval;
    private Integer freeSpaceControlArea;
    private FRLogOption frlogOption;
    private DatasetOrganisation dataOrg;
    private int keyLength;
    private int keyOffset;
    private LogOption logOption;
    private String logStreamID;
    private String managementClass;
    private String modelEntryName;
    private String modelCatName;
    private String owner;
    private RecatalogOption recatalogOption;
    private int averageRecordSize;
    private int maxRecordSize;
    private ReuseOption reuseOption;
    private int crossRegionShareOption;
    private int crossSystemShareOption;
    private SpanOption spanOption;
    private SpeedRecoveryOption speedRecoveryOption;
    private String storageClass;
    private WriteCheckOption writeCheckOption;

    // Parameters for the data file
    private boolean useDATA;
    private boolean uniqueDATA;
    private String dataName;
    private VSAMSpaceUnit dataSpaceUnit;
    private int dataPrimaryExtents;
    private int dataSecondaryExtents;
    private String dataVolumes;
    private long dataBufferspace;
    private String dataControlInterval;
    private EraseOption dataEraseOption;
    private String dataExceptionExit;
    private Integer dataFreeSpaceControlInterval;
    private Integer dataFreeSpaceControlArea;
    private int dataKeyLength;
    private int dataKeyOffset;
    private String dataModelEntryName;
    private String dataModelCatName;
    private String dataOwner;
    private int dataAverageRecordSize;
    private int dataMaxRecordSize;
    private ReuseOption dataReuseOption;
    private int dataCrossRegionShareOption;
    private int dataCrossSystemShareOption;
    private SpanOption dataSpanOption;
    private SpeedRecoveryOption dataSpeedRecoveryOption;
    private WriteCheckOption dataWriteCheckOption;

    // Parameters for the index file
    private boolean useINDEX;
    private boolean uniqueINDEX;
    private String indexName;
    private VSAMSpaceUnit indexSpaceUnit;
    private int indexPrimaryExtents;
    private int indexSecondaryExtents;
    private String indexVolumes;
    private String indexControlInterval;
    private String indexExceptionExit;
    private String indexModelEntryName;
    private String indexModelCatName;
    private String indexOwner;
    private ReuseOption indexReuseOption;
    private int indexCrossRegionShareOption;
    private int indexCrossSystemShareOption;
    private WriteCheckOption indexWriteCheckOption;

    // Parameters for the catalog
    private boolean useCATALOG;
    private String catalog;

    private String idcamsInput;
    private String idcamsOutput;
    private String idcamsCommand;
    private int idcamsRc;
    
    private DatasetDataType dataType = DatasetDataType.TEXT;

    private boolean shouldArchive = true;

    private static int temporaryQualifierCounter = 0;

    // Abbreviations used as parameters in the IDCAMS commands
    private static final String PARM_NAME = "NAME";
    private static final String PARM_VOLUMES = "VOL";
    private static final String PARM_ACCOUNT = "ACCT";
    private static final String PARM_BUFFERSPACE = "BUFSP";
    private static final String PARM_BACKUP_WHILE_OPEN = "BWO";
    private static final String PARM_CATALOG = "CAT";
    private static final String PARM_CONTROLINTERVALSIZE = "CISZ";
    private static final String PARM_DATACLASS = "DATACLAS";
    private static final String PARM_EXCEPTIONEXIT = "EEXT";
    private static final String PARM_FREESPACE = "FSPC";
    private static final String PARM_FRLOG = "FRLOG";
    private static final String PARM_KEYS = "KEYS";
    private static final String PARM_LOG = "LOG";
    private static final String PARM_LOGSTREAMID = "LSID"; //clash linear
    private static final String PARM_MANAGEMENTCLASS = "MGMTCLAS";
    private static final String PARM_MODEL = "MODEL";
    private static final String PARM_OWNER = "OWNER";
    private static final String PARM_RECORDSIZE = "RECSZ";
    private static final String PARM_SHAREOPTIONS = "SHR";
    private static final String PARM_STORAGECLASS = "STORCLAS";
    
    private static final String PROP_INVOCATION = "invocation";
    private static final String PROP_PATH = "path";
    private static final String PROP_OUTPUT = "output";
    private static final String PROP_STDOUT = "stdout";
    private static final String PROP_EXIT_CODE = "exit code";

    // Default LLQs for index and data files
    private static final String LLQ_DATA = ".DATA";
    private static final String LLQ_INDEX = ".INDEX";
    
    private static final String LOG_VSAM_DATA_SET = "VSAM data set ";
    private static final String LOG_ARCHIVED_TO = " archived to ";
    private static final String LOG_DOES_NOT_EXIST = " does not exist";
    private static final String LOG_UNABLE_TO_DELETE_REPRO_DATASET = "Unable to delete IDCAMS REPRO temporary dataset";
    private static final String LOG_UNABLE_TO_RETRIEVE_CONTENT_FROM_REPRO_DATASET = "Unable to retrieve content from IDCAMS REPRO temporary dataset";
    private static final String LOG_UNABLE_TO_FIND = "Unable to find \"";
    private static final String LOG_MEMBER_IN_RESPONSE_BODY = "\" member in response body";

    private static final Log logger = LogFactory.getLog(RseapiZosVSAMDatasetImpl.class);

    public RseapiZosVSAMDatasetImpl(RseapiZosFileHandlerImpl zosFileHandler, IZosImage image, String dsname) throws ZosVSAMDatasetException {
        this.zosFileHandler = zosFileHandler;
        try {
            this.image = image;
            this.name = dsname;
            this.dataName = name + LLQ_DATA;
            this.indexName = name + LLQ_INDEX;
            this.zosDataset = (RseapiZosDatasetImpl) this.zosFileHandler.newDataset(this.name, this.image);
        } catch (ZosDatasetException e) {
            throw new ZosVSAMDatasetException(e);
        }
        this.rseapiApiProcessor = this.zosDataset.getRseapiApiProcessor();
    }

    @Override
    public IZosVSAMDataset create() throws ZosVSAMDatasetException {
        if (exists()) {
            throw new ZosVSAMDatasetException(LOG_VSAM_DATA_SET + quoted(this.name) + " already exists" + logOnImage());
        }
        
        idcamsRequest(getDefineCommand());
        
        if (exists()) {
            String retained = "";
            if (this.retainToTestEnd) {
                retained = " and will be retained until the end of this test run";
            }
            logger.info(LOG_VSAM_DATA_SET + quoted(this.name) + " created" + logOnImage() + retained);
            this.datasetCreated = true;
        } else {
            throw new ZosVSAMDatasetException(LOG_VSAM_DATA_SET + quoted(this.name) + " not created" + logOnImage());
        }
        
        return this;
    }

    @Override
    public IZosVSAMDataset createRetain() throws ZosVSAMDatasetException {
        this.retainToTestEnd = true;
        return create();
    }

    @Override
    public boolean delete() throws ZosVSAMDatasetException {
        if (!exists()) {
            throw new ZosVSAMDatasetException(LOG_VSAM_DATA_SET + quoted(this.name) + LOG_DOES_NOT_EXIST + logOnImage());
        }
        
        idcamsRequest(getDeleteCommand());
            
        if (exists()) {
            logger.info(LOG_VSAM_DATA_SET + quoted(this.name) + " not deleted" + logOnImage());
            return false;
        } else {
            logger.info(LOG_VSAM_DATA_SET + quoted(this.name) + " deleted" + logOnImage());
            return true;
        }
    }

    @Override
    public boolean exists() throws ZosVSAMDatasetException {
        try {
            return this.zosDataset.exists();
        } catch (ZosDatasetException e) {
            throw new ZosVSAMDatasetException(e);
        }        
    }

    @Override
    public void storeText(String content) throws ZosVSAMDatasetException {
        RseapiZosDatasetImpl fromDataset = createReproDataset(content);
        store(fromDataset);
        try {
            fromDataset.delete();
        } catch (ZosDatasetException e) {
            throw new ZosVSAMDatasetException(LOG_UNABLE_TO_DELETE_REPRO_DATASET, e);
        }
    }

    @Override
    public void storeBinary(byte[] content) throws ZosVSAMDatasetException {
        RseapiZosDatasetImpl fromDataset = createReproDataset(content);
        store(fromDataset);
        try {
            fromDataset.delete();
        } catch (ZosDatasetException e) {
            throw new ZosVSAMDatasetException(LOG_UNABLE_TO_DELETE_REPRO_DATASET, e);
        }
    }

    @Override
    public void store(IZosDataset fromDataset) throws ZosVSAMDatasetException {
        if (!exists()) {
            throw new ZosVSAMDatasetException(LOG_VSAM_DATA_SET + quoted(this.name) + LOG_DOES_NOT_EXIST + logOnImage());
        }
        try {
            if (!fromDataset.exists()) {
                throw new ZosVSAMDatasetException("From data set " + quoted(fromDataset.getName()) + LOG_DOES_NOT_EXIST + logOnImage());
            }
        } catch (ZosDatasetException e) {
            throw new ZosVSAMDatasetException(e);
        }
        
        idcamsRequest(getReproFromCommand(fromDataset.getName()));
    }

    @Override
    public String retrieveAsText() throws ZosVSAMDatasetException {
        if (!exists()) {
            throw new ZosVSAMDatasetException(LOG_VSAM_DATA_SET + quoted(this.name) + LOG_DOES_NOT_EXIST + logOnImage());
        }

        RseapiZosDatasetImpl toDataset = createReproDataset(null);
        
        idcamsRequest(getReproToCommand(toDataset.getName()));
        
        String content = null;
        try {
            content = toDataset.retrieveAsText();
        } catch (ZosDatasetException e) {
            throw new ZosVSAMDatasetException(LOG_UNABLE_TO_RETRIEVE_CONTENT_FROM_REPRO_DATASET, e);
        }
        try {
            toDataset.delete();
        } catch (ZosDatasetException e) {
            throw new ZosVSAMDatasetException(LOG_UNABLE_TO_DELETE_REPRO_DATASET, e);
        }
        return content;
    }

    @Override
    public byte[] retrieveAsBinary() throws ZosVSAMDatasetException {
        if (!exists()) {
            throw new ZosVSAMDatasetException(LOG_VSAM_DATA_SET + quoted(this.name) + LOG_DOES_NOT_EXIST + logOnImage());
        }

        RseapiZosDatasetImpl toDataset = createReproDataset(null);
        
        idcamsRequest(getReproToCommand(createReproDataset(null).getName()));
        
        byte[] content = null;
        try {
            content = toDataset.retrieveAsBinary();
        } catch (ZosDatasetException e) {
            throw new ZosVSAMDatasetException(LOG_UNABLE_TO_RETRIEVE_CONTENT_FROM_REPRO_DATASET, e);
        }
        try {
            toDataset.delete();
        } catch (ZosDatasetException e) {
            throw new ZosVSAMDatasetException(LOG_UNABLE_TO_DELETE_REPRO_DATASET, e);
        }
        return content;
    }

    @Override
    public void saveToResultsArchive(String rasPath) throws ZosVSAMDatasetException {
    	if (!shouldArchive()) {
    		throw new ZosVSAMDatasetException("shouldArchive flag is false");
    	}
        try {
            if (exists()) {
            	Path artifactPath = this.zosFileHandler.getZosFileManager().getVsamDatasetCurrentTestMethodArchiveFolder();
				String fileName = this.zosFileHandler.getZosManager().buildUniquePathName(artifactPath, this.name);
                try {
                    if (getTotalRecords() == 0) {
                    	this.zosFileHandler.getZosManager().storeArtifact(artifactPath.resolve(fileName), this.name, ResultArchiveStoreContentType.TEXT);
                    } else {
                    	if (this.dataType.equals(DatasetDataType.TEXT)) {
                    		this.zosFileHandler.getZosManager().storeArtifact(artifactPath.resolve(fileName), retrieveAsText(), ResultArchiveStoreContentType.TEXT);
                    	} else  {
                    		this.zosFileHandler.getZosManager().storeArtifact(artifactPath.resolve(fileName), new String(retrieveAsBinary()), ResultArchiveStoreContentType.TEXT);
                    	}
                    }
    			} catch (ZosManagerException e) {
    				throw new ZosDatasetException(e);
    			}
                
                logger.info(quoted(this.name) + LOG_ARCHIVED_TO + artifactPath.resolve(fileName));
            }
        } catch (ZosFileManagerException e) {
            logger.error("Unable to save VSAM data set to archive", e);
        }
    }
    
    private int getTotalRecords() {
        try {
			return Integer.valueOf(getValueFromListcat("REC-TOTAL"));
		} catch (NumberFormatException e) {
			// NOP
		} catch (ZosVSAMDatasetException e) {
			logger.error("Unable to get value of REC-TOTAL from LISTCAT output", e);
		}
		return 0;
	}

	@Override
    public void setDataType(DatasetDataType dataType) {
        logger.info("Data type set to " + dataType.toString());
        this.dataType = dataType;
    }

    @Override
    public void setSpace(VSAMSpaceUnit spaceUnit, int primaryExtents, int secondaryExtents) {
        this.spaceUnit = spaceUnit;
        this.primaryExtents = primaryExtents;
        this.secondaryExtents = secondaryExtents;
    }

    @Override
    public void setVolumes(String volumes) {
        this.volumes = volumes;
    }

    @Override
    public void setAccountInfo(String accountInfo) {
        this.accountInfo = accountInfo;
    }

    @Override
    public void setBufferspace(long bufferspace) {
        this.bufferspace = bufferspace;
    }

    @Override
    public void setBwoOption(BWOOption bwoOption) {
        this.bwoOption = bwoOption;
    }

    @Override
    public void setControlInterval(String controlInterval) {
        this.controlInterval = controlInterval;
    }

    @Override
    public void setDataclass(String dataclass) {
        this.dataclass = dataclass;
    }

    @Override
    public void setEraseOption(EraseOption eraseOption) {
        this.eraseOption = eraseOption;
    }

    @Override
    public void setExceptionExit(String exceptionExit) {
        this.exceptionExit = exceptionExit;
    }

    @Override
    public void setFreeSpaceOptions(int controlIntervalPercent, int controlAreaPercent) {
        this.freeSpaceControlInterval = controlIntervalPercent;
        this.freeSpaceControlArea = controlAreaPercent;
    }

    @Override
    public void setFrlogOption(FRLogOption frlogOption) {
        this.frlogOption = frlogOption;
    }

    @Override
    public void setDatasetOrg(DatasetOrganisation dataOrg) {
        this.dataOrg = dataOrg;
    }

    @Override
    public void setKeyOptions(int length, int offset) {
        this.keyLength = length;
        this.keyOffset = offset;
    }

    @Override
    public void setLogOption(LogOption logOption) {
        this.logOption = logOption;
    }

    @Override
    public void setLogStreamID(String logStreamID) {
        this.logStreamID = logStreamID;
    }

    @Override
    public void setManagementClass(String managementClass) {
        this.managementClass = managementClass;
    }

    @Override
    public void setModel(String modelEntryName, String modelCatName) {
        this.modelEntryName = modelEntryName;
        this.modelCatName = modelCatName;
    }

    @Override
    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public void setRecatalogOption(RecatalogOption recatalogOption) {
        this.recatalogOption = recatalogOption;
    }

    @Override
    public void setRecordSize(int average, int max) {
        this.averageRecordSize = average;
        this.maxRecordSize = max;
    }

    @Override
    public void setReuseOption(ReuseOption reuseOption) {
        this.reuseOption = reuseOption;
    }

    @Override
    public void setShareOptions(int crossRegion, int crossSystem) {
        this.crossRegionShareOption = crossRegion;
        this.crossSystemShareOption = crossSystem;
    }

    @Override
    public void setSpanOption(SpanOption spanOption) {
        this.spanOption = spanOption;
    }

    @Override
    public void setSpeedRecoveryOption(SpeedRecoveryOption speedRecoveryOption) {
        this.speedRecoveryOption = speedRecoveryOption;
    }

    @Override
    public void setStorageClass(String storageClass) {
        this.storageClass = storageClass;
    }

    @Override
    public void setWriteCheckOption(WriteCheckOption writeCheckOption) {
        this.writeCheckOption = writeCheckOption;
    }

    @Override
    public void setUseDATA(boolean useDATA, boolean unique) {
        this.useDATA = useDATA;
        this.uniqueDATA = unique;
    }

    @Override
    public void setDataName(String dataName) {
        this.dataName = dataName;
    }

    @Override
    public void setDataSpace(VSAMSpaceUnit spaceUnit, int primaryExtents, int secondaryExtents) {
        this.dataSpaceUnit = spaceUnit;
        this.dataPrimaryExtents = primaryExtents;
        this.dataSecondaryExtents = secondaryExtents;
    }

    @Override
    public void setDataVolumes(String dataVolumes) {
        this.dataVolumes = dataVolumes;
    }

    @Override
    public void setDataBufferspace(long dataBufferspace) {
        this.dataBufferspace = dataBufferspace;
    }

    @Override
    public void setDataControlInterval(String dataControlInterval) {
        this.dataControlInterval = dataControlInterval;
    }

    @Override
    public void setDataEraseOption(EraseOption dataEraseOption) {
        this.dataEraseOption = dataEraseOption;
    }

    @Override
    public void setDataExceptionExit(String dataExceptionExit) {
        this.dataExceptionExit = dataExceptionExit;
    }

    @Override
    public void setDataFreeSpaceOptions(int controlIntervalPercent, int controlAreaPercent) {
        this.dataFreeSpaceControlInterval = controlIntervalPercent;
        this.dataFreeSpaceControlArea = controlAreaPercent;
    }

    @Override
    public void setDataKeyOptions(int length, int offset) {
        this.dataKeyLength = length;
        this.dataKeyOffset = offset;
    }

    @Override
    public void setDataModel(String dataModelEntryName, String dataModelCatName) {
        this.dataModelEntryName = dataModelEntryName;
        this.dataModelCatName = dataModelCatName;
    }

    @Override
    public void setDataOwner(String dataOwner) {
        this.dataOwner = dataOwner;
    }

    @Override
    public void setDataRecordSize(int average, int max) {
        this.dataAverageRecordSize = average;
        this.dataMaxRecordSize = max;
    }

    @Override
    public void setDataReuseOption(ReuseOption dataReuseOption) {
        this.dataReuseOption = dataReuseOption;
    }

    @Override
    public void setDataShareOptions(int crossRegion, int crossSystem) {
        this.dataCrossRegionShareOption = crossRegion;
        this.dataCrossSystemShareOption = crossSystem;
    }

    @Override
    public void setDataSpanOption(SpanOption dataSpanOption) {
        this.dataSpanOption = dataSpanOption;
    }

    @Override
    public void setDataSpeedRecoveryOption(
            SpeedRecoveryOption dataSpeedRecoveryOption) {
        this.dataSpeedRecoveryOption = dataSpeedRecoveryOption;
    }

    @Override
    public void setDataWriteCheckOption(WriteCheckOption dataWriteCheckOption) {
        this.dataWriteCheckOption = dataWriteCheckOption;
    }

    @Override
    public void setUseINDEX(boolean useINDEX, boolean unique) {
        this.useINDEX = useINDEX;
        this.uniqueINDEX = unique;
    }

    @Override
    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    @Override
    public void setIndexSpace(VSAMSpaceUnit spaceUnit, int primaryExtents, int secondaryExtents) {
        this.indexSpaceUnit = spaceUnit;
        this.indexPrimaryExtents = primaryExtents;
        this.indexSecondaryExtents = secondaryExtents;
    }

    @Override
    public void setIndexVolumes(String indexVolumes) {
        this.indexVolumes = indexVolumes;
    }

    @Override
    public void setIndexControlInterval(String indexControlInterval) {
        this.indexControlInterval = indexControlInterval;
    }

    @Override
    public void setIndexExceptionExit(String indexExceptionExit) {
        this.indexExceptionExit = indexExceptionExit;
    }

    @Override
    public void setIndexModel(String indexModelEntryName, String indexModelCatName) {
        this.indexModelEntryName = indexModelEntryName;
        this.indexModelCatName = indexModelCatName;
    }

    @Override
    public void setIndexOwner(String indexOwner) {
        this.indexOwner = indexOwner;
    }

    @Override
    public void setIndexReuseOption(ReuseOption indexReuseOption) {
        this.indexReuseOption = indexReuseOption;
    }

    @Override
    public void setIndexShareOptions(int crossRegion, int crossSystem) {
        this.indexCrossRegionShareOption = crossRegion;
        this.indexCrossSystemShareOption = crossSystem;
    }

    @Override
    public void setIndexWriteCheckOption(WriteCheckOption indexWriteCheckOption) {
        this.indexWriteCheckOption = indexWriteCheckOption;
    }

    @Override
    public void setCatalog(String catalog) {
        this.useCATALOG = true;
        this.catalog = catalog;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public DatasetDataType getDataType() {
        return this.dataType;
    }

    @Override
    public String getCommandInput() {
        return this.idcamsInput;
    }

    @Override
    public String getCommandOutput() {
        return this.idcamsOutput;
    }

    @Override
    public String getDefineCommand() throws ZosVSAMDatasetException {
    
        StringBuilder sb = new StringBuilder();
        sb.append("DEFINE -\n");
        sb.append("  CLUSTER(");
    
        appendParameter(PARM_NAME, "'" + name + "'", sb, true);
        appendParameter(PARM_VOLUMES, volumes, sb, false);
        appendParameter(spaceUnit, primaryExtents + " " + secondaryExtents, sb);
        appendParameter(PARM_ACCOUNT, accountInfo, sb);
        appendParameter(PARM_BACKUP_WHILE_OPEN, bwoOption, sb);
        appendParameter(PARM_BUFFERSPACE, ((bufferspace > 0) ? bufferspace : null), sb);
        appendParameter(PARM_CONTROLINTERVALSIZE, controlInterval, sb);
        appendParameter(PARM_DATACLASS, dataclass, sb);
        appendDeclaration(eraseOption, sb);
        appendParameter(PARM_EXCEPTIONEXIT, exceptionExit, sb);
        appendParameter(PARM_FREESPACE, (freeSpaceControlInterval != null ? freeSpaceControlInterval + " " + freeSpaceControlArea : null), sb);
        appendParameter(PARM_FRLOG, frlogOption, sb);
        appendDeclaration(dataOrg, sb);
        appendParameter(PARM_KEYS, ((keyLength > 0) ? keyLength + " " + keyOffset : null), sb);
        appendParameter(PARM_LOG, logOption, sb);
        appendParameter(PARM_LOGSTREAMID, logStreamID, sb);
        String parameterValue = modelCatName != null ? " " + modelCatName : "";
        appendParameter(PARM_MANAGEMENTCLASS, managementClass, sb);
        appendParameter(PARM_MODEL, ((modelEntryName != null) ? modelEntryName + parameterValue : null), sb);
        appendParameter(PARM_OWNER, owner, sb);
        appendDeclaration(recatalogOption, sb);
        parameterValue = maxRecordSize > 0 ? " " + maxRecordSize : "";
        appendParameter(PARM_RECORDSIZE, ((averageRecordSize > 0) ? averageRecordSize + parameterValue : null), sb);
        appendDeclaration(reuseOption, sb);
        parameterValue = crossSystemShareOption > 0 ? " " + crossSystemShareOption : "";
        appendParameter(PARM_SHAREOPTIONS, ((crossRegionShareOption > 0) ? crossRegionShareOption + parameterValue : null), sb);
        appendDeclaration(spanOption, sb);
        appendDeclaration(speedRecoveryOption, sb);
        appendParameter(PARM_STORAGECLASS, storageClass, sb);
        appendDeclaration(writeCheckOption, sb);
    
        sb.append("  )");
    
        if (useDATA) {
            getDataDefineCommand(sb);
        }
    
        if (useINDEX) {
            getIndexDefineCommand(sb);
        }
    
        if (useCATALOG) {
            appendParameter(PARM_CATALOG, catalog, sb);
        }
    
        return StringUtils.stripEnd(sb.toString(), " -\n");
    }

    @Override
    public String getDeleteCommand() throws ZosVSAMDatasetException {
        return "DELETE -\n  '" + this.name + "' -\n  PURGE";
    }

    @Override
    public String getReproToCommand(String outDatasetName) {
        return "REPRO -\n  INDATASET('" + this.name + "') -\n  OUTDATASET('" + outDatasetName + "')";
    }

    @Override
    public String getReproFromCommand(String inDatasetName) {
        return "REPRO -\n  INDATASET('" + inDatasetName + "') -\n  OUTDATASET('" + this.name + "')";
    }

    @Override
    public String getListcatOutput() throws ZosVSAMDatasetException {
        if (!exists()) {
            throw new ZosVSAMDatasetException(LOG_VSAM_DATA_SET + quoted(this.name) + LOG_DOES_NOT_EXIST + logOnImage());
        }
        
        idcamsRequest(getListcatCommand());
        
        return this.idcamsOutput;
    }

    @Override
    public String getAttibutesAsString() throws ZosVSAMDatasetException {
        if (!exists()) {
            return getDefineCommand();
        }
        return getListcatOutput();
    }

    @Override
	public void setShouldArchive(boolean shouldArchive) {
		this.shouldArchive = shouldArchive;
	}

	@Override
	public boolean shouldArchive() {
		return this.shouldArchive;
	}

    protected RseapiZosDatasetImpl createReproDataset(Object content) throws ZosVSAMDatasetException {
        String reproDsname;
        try {
            reproDsname = this.zosFileHandler.getZosFileManager().getRunDatasetHLQ(this.image) + "." + temporaryLLQ();
        } catch (ZosFileManagerException e) {
            throw new ZosVSAMDatasetException(e);
        }
    
        RseapiZosDatasetImpl reproDataset = null;
        try {
            reproDataset = (RseapiZosDatasetImpl) this.zosFileHandler.newDataset(reproDsname, this.image);
            reproDataset.setDatasetOrganization(DatasetOrganization.SEQUENTIAL);
            reproDataset.setRecordFormat(RecordFormat.VARIABLE_BLOCKED);
            int recordLength = this.maxRecordSize == 0 ? Integer.valueOf(getValueFromListcat("MAXLRECL")) + 4 : this.maxRecordSize + 4;
            reproDataset.setRecordlength(recordLength);
            reproDataset.setBlockSize(0);
            String spaceType = getValueFromListcat("SPACE-TYPE");
            int spacePri = Integer.parseInt(getValueFromListcat("SPACE-PRI"));
            int spaceSec = Integer.parseInt(getValueFromListcat("SPACE-SEC"));
            reproDataset.setSpace(SpaceUnit.valueOf(spaceType + "S"), spacePri, spaceSec);
            reproDataset.createTemporary();
            reproDataset.setDataType(this.dataType);
            if (content != null) {
                if (content instanceof String) {
                    reproDataset.storeText((String) content);
                } else if (content instanceof byte[]) {
                    reproDataset.storeBinary((byte[]) content);
                } else {
                    throw new ZosVSAMDatasetException("Invalid content type - " + content.getClass().getName());
                }
            }
        } catch (ZosDatasetException e) {
            throw new ZosVSAMDatasetException("Unable to create temporary dataset for IDCAMS REPRO", e);
        }
        return reproDataset;
    }

    protected String temporaryLLQ() {
        return this.zosFileHandler.getZosFileManager().getRunId() + ".T" + StringUtils.leftPad(String.valueOf(++temporaryQualifierCounter), 4, "0");
    }

    protected String getValueFromListcat(String findString) throws ZosVSAMDatasetException {
        if (!"LISTCAT".equals(this.idcamsCommand)) {
            getListcatOutput();
        }
        int start = this.idcamsOutput.indexOf(findString);
        if (start < 0) {
            throw new ZosVSAMDatasetException(LOG_UNABLE_TO_FIND + findString + "\" in LISTCAT output");
        }
        int end = this.idcamsOutput.indexOf(' ', start);
        String field = this.idcamsOutput.substring(start, end);
        String[] splitField = field.split("-");
        return splitField[splitField.length-1].replace("\n", "");
            
    }

    protected void parseOutput(JsonObject responseBody) throws ZosVSAMDatasetException {
    	logger.trace(responseBody);
    	int exitCode = 0;
    	JsonElement ec = responseBody.get(PROP_EXIT_CODE);
    	if (ec == null) {
    		throw new ZosVSAMDatasetException(LOG_UNABLE_TO_FIND + PROP_EXIT_CODE + LOG_MEMBER_IN_RESPONSE_BODY);
    	}
    	exitCode = ec.getAsInt();
    	if (exitCode == 0) {
    		JsonElement output = responseBody.get(PROP_OUTPUT);
    		if (output == null) {
        		throw new ZosVSAMDatasetException(LOG_UNABLE_TO_FIND + PROP_OUTPUT + LOG_MEMBER_IN_RESPONSE_BODY);
        	}
    		JsonElement stdout = output.getAsJsonObject().get(PROP_STDOUT);
    		if (stdout == null) {
        		throw new ZosVSAMDatasetException(LOG_UNABLE_TO_FIND + PROP_STDOUT + LOG_MEMBER_IN_RESPONSE_BODY);
        	}
    		this.idcamsOutput = stdout.getAsString();
    		logger.debug("IDCAMS Output:\n" + this.idcamsOutput);
    	}
    	setIdcamsRc();
    }

    protected int setIdcamsRc() throws ZosVSAMDatasetException {
    	this.idcamsRc = 0;
    	if (this.idcamsOutput == null) {
    		throw new ZosVSAMDatasetException("Unable to parse IDCAMS output for LASTCC - no output");
    	}
    	int lastccPos = this.idcamsOutput.lastIndexOf("LASTCC=");
    	if (lastccPos > -1) {
    		String lastccStr = this.idcamsOutput.substring(lastccPos + 7).trim();
    		if (!lastccStr.isEmpty()) {
    			this.idcamsRc = Integer.valueOf(lastccStr);
    		}
    	}
    	return this.idcamsRc;
	}

	protected void idcamsRequest(String request) throws ZosVSAMDatasetException {
        this.idcamsCommand = request.substring(0, request.indexOf(' '));
        this.idcamsInput = request;
        this.idcamsOutput = null;
        this.idcamsRc = -9999;
        
        String command = "tsocmd \"" + formatRequest(request) + "\"";
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty(PROP_INVOCATION, command);
        requestBody.addProperty(PROP_PATH, "/usr/bin");
   
        IRseapiResponse response;
        try {
			response = this.rseapiApiProcessor.sendRequest(RseapiRequestType.POST_JSON, RESTUNIXCOMMANDS_PATH, null, requestBody, RseapiZosFileHandlerImpl.VALID_STATUS_CODES, false);
        } catch (RseapiException e) {
            throw new ZosVSAMDatasetException(e);
        }

        if (response.getStatusCode() != HttpStatus.SC_OK) {
        	// Error case
            String displayMessage = this.zosFileHandler.buildErrorString("zOS UNIX command", response); 
            logger.error(displayMessage);
            throw new ZosVSAMDatasetException(displayMessage);
        }
        
        JsonObject responseBody;
        try {
            responseBody = response.getJsonContent();
        } catch (RseapiException e) {
            throw new ZosVSAMDatasetException("Unable to execute IDCAMS command", e);
        }
        
        parseOutput(responseBody);
        if (this.idcamsRc > 0) {
        	if (this.idcamsRc > 4) {
        		String displayMessage = "IDCAMS processing failed: RC=" + this.idcamsRc + "\n" + this.idcamsOutput;
        		logger.error(displayMessage);
        		throw new ZosVSAMDatasetException(displayMessage);
        	} else {
        		logger.warn("WARNING: IDCAMS RC=" + this.idcamsRc);
        	}
        }
    }

	protected String formatRequest(String request) {
		logger.debug("IDCAMS input:\n" + request);
		return request.replaceAll("[-\\n]", "").
			           replaceAll("\\s{2,}", " ").
			           trim();
	}

	protected void getDataDefineCommand(StringBuilder sb) throws ZosVSAMDatasetException {
        sb.append("  DATA( ");
    
        appendParameter(PARM_NAME, "'" + dataName + "'", sb);
        appendParameter(PARM_VOLUMES, dataVolumes, sb);
        appendParameter(dataSpaceUnit, ((dataPrimaryExtents > 0) ? dataPrimaryExtents + " " + dataSecondaryExtents : null), sb);
        appendParameter(PARM_BUFFERSPACE, ((dataBufferspace > 0) ? dataBufferspace : null), sb);
        appendParameter(PARM_CONTROLINTERVALSIZE, dataControlInterval, sb);
        appendDeclaration(dataEraseOption, sb);
        appendParameter(PARM_EXCEPTIONEXIT, dataExceptionExit, sb);
        appendParameter(PARM_FREESPACE, (dataFreeSpaceControlInterval != null ? dataFreeSpaceControlInterval + " " + dataFreeSpaceControlArea : null), sb);
        appendParameter(PARM_KEYS, ((dataKeyLength > 0) ? dataKeyLength + " " + dataKeyOffset : null), sb);
        String parameterValue = dataModelCatName != null ? " " + dataModelCatName : "";
        appendParameter(PARM_MODEL, ((dataModelEntryName != null) ? dataModelEntryName + parameterValue : null), sb);
        appendParameter(PARM_OWNER, dataOwner, sb);
        parameterValue = dataMaxRecordSize > 0 ? " " + dataMaxRecordSize : "";
        appendParameter(PARM_RECORDSIZE, ((dataAverageRecordSize > 0) ? dataAverageRecordSize + parameterValue : null), sb);
        appendDeclaration(dataReuseOption, sb);
        parameterValue = dataCrossSystemShareOption > 0 ? " " + dataCrossSystemShareOption : "";
        appendParameter(PARM_SHAREOPTIONS, ((dataCrossRegionShareOption > 0) ? dataCrossRegionShareOption + parameterValue : null), sb);
        appendDeclaration(dataSpanOption, sb);
        appendDeclaration(dataSpeedRecoveryOption, sb);
        appendDeclaration(dataWriteCheckOption, sb);
    
        sb.append("  " + ((uniqueDATA) ? "UNIQUE" : "") + ")");
    }

    protected void getIndexDefineCommand(StringBuilder sb) throws ZosVSAMDatasetException {
        sb.append("  INDEX( ");

        appendParameter(PARM_NAME, "'" + indexName + "'", sb);
        appendParameter(PARM_VOLUMES, indexVolumes, sb);
        appendParameter(indexSpaceUnit, ((indexPrimaryExtents > 0) ? indexPrimaryExtents + " " + indexSecondaryExtents : null), sb);
        appendParameter(PARM_CONTROLINTERVALSIZE, indexControlInterval, sb);
        appendParameter(PARM_EXCEPTIONEXIT, indexExceptionExit, sb);
        String parameterValue = indexModelCatName != null ? " " + indexModelCatName : "";
        appendParameter(PARM_MODEL, ((indexModelEntryName != null) ? indexModelEntryName + parameterValue : null), sb);
        appendParameter(PARM_OWNER, indexOwner, sb);
        appendDeclaration(indexReuseOption, sb);
        parameterValue = indexCrossSystemShareOption > 0 ? " " + indexCrossSystemShareOption : "";
        appendParameter(PARM_SHAREOPTIONS, ((indexCrossRegionShareOption > 0) ? indexCrossRegionShareOption + parameterValue : null), sb);
        appendDeclaration(indexWriteCheckOption, sb);

        sb.append("  " + ((uniqueINDEX) ? "UNIQUE" : "") + ")");
    }

    /**
     * Add a declaration to the define command, such as NOERASE
     * 
     * @param value
     * @param sb
     */
    protected void appendDeclaration(Object value, StringBuilder sb) {
        if (value != null) {
            sb.append(" " + value);
        }
    }

    /**
     * Add an optional parameter to the define command
     * 
     * @param parameter
     * @param value
     * @param sb
     * @throws ZosFileException
     */
    protected void appendParameter(Object parameter, Object value, StringBuilder sb) throws ZosVSAMDatasetException {
        appendParameter(parameter, value, sb, false);
    }

    /**
     * Add a parameter to the define command
     * 
     * @param parameter
     * @param value
     * @param sb
     * @param required
     * @throws ZosFileException
     */
    protected void appendParameter(Object parameter, Object value, StringBuilder sb, boolean required) throws ZosVSAMDatasetException {
        if ((parameter != null) && (value != null)) {
            sb.append("  " + parameter + "(" + value + ") -\n");
        } else {
            if (required) {
                throw new ZosVSAMDatasetException("Required parameter '" + parameter + "' has not been set");
            }
        }
    }

    protected String getListcatCommand() {
        return "LISTCAT -\n  ENTRY ('" + this.name + "') ALL";
    }

    protected String quoted(String name) {
        return "\"" + name + "\"";
    }

    protected String logOnImage() {
        return " on image " + this.image.getImageID();
    }

    @Override
    public String toString() {
        return this.name;
    }

    public boolean created() {
        return this.datasetCreated;
    }
    
    public boolean retainToTestEnd() {
        return this.retainToTestEnd;
    }
}
