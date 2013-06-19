package org.motechproject.commcare.domain;

import java.util.List;

public class TopLevelCaseResponse {

    private CasesMeta meta;
    private List<CaseResponseJson> objects;
    public CasesMeta getMeta() {
        return meta;
    }
    public void setMeta(CasesMeta meta) {
        this.meta = meta;
    }
    public List<CaseResponseJson> getObjects() {
        return objects;
    }
    public void setObjects(List<CaseResponseJson> objects) {
        this.objects = objects;
    }
}
