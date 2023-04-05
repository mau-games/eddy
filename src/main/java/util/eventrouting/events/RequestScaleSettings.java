package util.eventrouting.events;

import gui.views.ScaleViewController;
import util.eventrouting.PCGEvent;

public class RequestScaleSettings extends PCGEvent {

    private String strSizeAdjType;
    private String strScaleType;
    private double scaleFactor;
    private String firstDimType;
    private String secDimType;

    //Used for initializing dimension types that should be preserved from the original room
    public RequestScaleSettings(String strSizeAdjType, String strScaleType, double scaleFactor, String firstDimType, String secDimType){
        this.strSizeAdjType = strSizeAdjType;
        this.strScaleType = strScaleType;
        this.scaleFactor = scaleFactor;
        this.firstDimType = firstDimType;
        this.secDimType = secDimType;
    }
    public RequestScaleSettings(String strScaleType, double scaleFactor){
        this.strScaleType = strScaleType;
        this.scaleFactor = scaleFactor;
    }

    public String getStrSizeAdjType() {
        return strSizeAdjType;
    }
    public void setStrScaleType(String strScaleType) {
        this.strScaleType = strScaleType;
    }
    public String getStrScaleType(){
        return strScaleType;
    }
    public void setStrScaleType(){
        this.strScaleType = strScaleType;
    }
    public double getScaleFactor(){
        return scaleFactor;
    }
    public void setScaleFactor(double scaleFactor){
        this.scaleFactor = scaleFactor;
    }
    public String getFirstDimType(){
        return firstDimType;
    }
    public void setFirstDimType(String firstDimType) {
        this.firstDimType = firstDimType;
    }
    public String getSecDimType(){
        return secDimType;
    }
    public void setSecDimType(String secDimType) {
        this.secDimType = secDimType;
    }
}
