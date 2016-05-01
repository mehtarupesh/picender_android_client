package com.grafixartist.gallery;

/**
 * Created by rupesh on 4/28/16.
 */
public class FileState {

    private Boolean selected;
    private Boolean onCloud;
    private Boolean markForDeletion;

    public FileState(Boolean selected, Boolean onCloud, Boolean markForDeletion) {
        this.selected = selected;
        this.onCloud = onCloud;
        this.markForDeletion = markForDeletion;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public Boolean getOnCloud() {
        return onCloud;
    }

    public void setOnCloud(Boolean onCloud) {
        this.onCloud = onCloud;
    }

    public Boolean getMarkForDeletion() {
        return markForDeletion;
    }

    public void setMarkForDeletion(Boolean markForDeletion) {
        this.markForDeletion = markForDeletion;
    }
}
