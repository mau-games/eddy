package util.eventrouting.events;

import gui.views.ScaleViewController;
import util.eventrouting.PCGEvent;

public class RequestScaleSettings extends PCGEvent {

    private String strSizeAdjType;
    private String strScaleType;
    private double scaleFactor;
    private String[] preserveDimArr;

    //Used for initializing dimension types that should be preserved from the original room
    public RequestScaleSettings(String strSizeAdjType, String strScaleType, double scaleFactor, String[] preserveDimArr){
        this.strSizeAdjType = strSizeAdjType;
        this.strScaleType = strScaleType;
        this.scaleFactor = scaleFactor;
        this.preserveDimArr = preserveDimArr;
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
    public String[] getDimTypes(){
        return preserveDimArr;
    }
    public void setFirstDimType(String[] preserveDimArr) {
        this.preserveDimArr = preserveDimArr;
    }

}
