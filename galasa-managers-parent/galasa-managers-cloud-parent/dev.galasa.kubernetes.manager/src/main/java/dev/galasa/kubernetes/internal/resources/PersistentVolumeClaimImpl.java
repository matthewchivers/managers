package dev.galasa.kubernetes.internal.resources;

import dev.galasa.kubernetes.IPersistentVolumeClaim;
import dev.galasa.kubernetes.internal.KubernetesNamespaceImpl;
import io.kubernetes.client.openapi.models.V1PersistentVolumeClaim;
import io.kubernetes.client.util.Yaml;

public class PersistentVolumeClaimImpl implements IPersistentVolumeClaim {
    
    private final V1PersistentVolumeClaim pvc;

    public PersistentVolumeClaimImpl(KubernetesNamespaceImpl namespace, V1PersistentVolumeClaim pvc) {
        this.pvc = pvc;
    }

    @Override
    public String getName() {
        return pvc.getMetadata().getName();
    }

    @Override
    public TYPE getType() {
        return TYPE.PersistentVolumeClaim;
    }

    @Override
    public String getYaml() {
        return Yaml.dump(this.pvc);
    }

}
