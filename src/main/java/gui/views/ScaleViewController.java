package gui.views;

import game.Room;
import util.algorithms.ScaleFibonacci;
import util.algorithms.NearestNeighbour;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.RequestScaleSettings;

public class ScaleViewController implements Listener{
    private Room scaleRoom;
    private NearestNeighbour nn;
    private ScaleFibonacci fib;
    private static EventRouter router = EventRouter.getInstance();

    public enum ScaleType{
        None,
        NearestNeighbour,
        Fibonacci
    }
    public enum SizeAdjustType{
        Upscale,
        Downscale
    }
    public ScaleViewController(Room scaleRoom){
        this.scaleRoom = scaleRoom;
        router.registerListener(this, new RequestScaleSettings(null, null, -1, null, null));

        System.out.println("ScaleViewController: " + scaleRoom.getRoomHeight() + " height. " + scaleRoom.getRoomWidth() + " width.");
    }
    @Override
    public synchronized void ping(PCGEvent e){
        if(e instanceof RequestScaleSettings){
            RequestScaleSettings rSS = (RequestScaleSettings)e;
            ScaleType scaleType = ScaleType.valueOf(rSS.getStrScaleType());
            SizeAdjustType sizeAdjustType = SizeAdjustType.valueOf(rSS.getStrSizeAdjType());
            boolean isUpscale = false;

            if(sizeAdjustType == SizeAdjustType.Upscale)
                isUpscale = true;

            switch (scaleType){
                case NearestNeighbour:
                    nn = new NearestNeighbour(scaleRoom.toMatrix(), (int)rSS.getScaleFactor(), isUpscale);
                    break;
                case Fibonacci:
                    fib = new ScaleFibonacci(scaleRoom.toMatrix(), rSS.getScaleFactor(), isUpscale);
                    break;
                case None:
                    System.out.println("None: scaletype");
                    break;
                default:
                    System.out.println("Invalid: scaletype");
                    break;

            }
        }
    }
}
