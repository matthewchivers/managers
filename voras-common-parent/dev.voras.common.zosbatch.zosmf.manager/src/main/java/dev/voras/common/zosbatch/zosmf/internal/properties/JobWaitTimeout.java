package dev.voras.common.zosbatch.zosmf.internal.properties;

import dev.voras.common.zosbatch.ZosBatchManagerException;
import dev.voras.framework.spi.ConfigurationPropertyStoreException;
import dev.voras.framework.spi.cps.CpsProperties;

/**
 * zOS Batch job execution wait timeout
 * <p>
 * The value in milliseconds to wait for the zOS Batch job execution to complete when submitted via zOSMF
 * </p><p>
 * The property is:<br>
 * {@code zosbatch.batchjob.[imageid].timeout=60000}
 * </p>
 * <p>
 * The default value is {@value #DEFAULT_JOB_WAIT_TIMEOUT}
 * </p>
 *
 */
public class JobWaitTimeout extends CpsProperties {

	private static final int DEFAULT_JOB_WAIT_TIMEOUT = 5 * 60 * 1000;

	public static int get(String imageId) throws ZosBatchManagerException {
		try {
			String timeoutString = getStringNulled(ZosBatchZosmfPropertiesSingleton.cps(), "batchjob", "timeout", imageId);

			if (timeoutString == null) {
				return DEFAULT_JOB_WAIT_TIMEOUT;
			} else {
				return Integer.parseInt(timeoutString);
			}
		} catch (ConfigurationPropertyStoreException e) {
			throw new ZosBatchManagerException("Problem asking the CPS for the batch job timeout property for zOS image "  + imageId, e);
		}
	}

}