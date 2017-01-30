package com.example.takashi.RailwayAndStationPuz.database;

/**
 * Created by takashi on 2017/01/06.
 */

public class CurrentMode {
    private int companyId;
    private boolean grid_display;

    public CurrentMode( int companyId, boolean grid_display){
        this.companyId = companyId;
        this.grid_display = grid_display;
    }

    public int getCompanyId(){
        return this.companyId;
    }
    public void setCompanyId( int companyId ){ this.companyId = companyId; }

    public boolean isGridDisplaied(){
        return this.grid_display;
    }
    public void setDisplayMode(boolean mode){
        this.grid_display = mode;
    }
}
