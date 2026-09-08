import dev.luma.visuals.HudMath;
import dev.luma.visuals.HudModule;
public class HudMathSmokeTest {
 static int checks=0;
 static void ok(boolean value,String what){checks++;if(!value)throw new AssertionError(what);}
 public static void main(String[] args){
  ok(HudMath.clamp(Double.NaN,0,1)==0,"NaN clamps safely");
  ok(HudMath.clamp(-5,0,1)==0,"Negative clamp");
  ok(HudMath.clamp(5,0,1)==1,"Upper clamp");
  ok(HudMath.normalized(40,100,100)==0,"Oversized card avoids division by zero");
  ok(HudMath.smooth(10,20,.016,true)==20,"Reduced motion is instant");
  ok(HudMath.smooth(10,20,.016,false)>10 && HudMath.smooth(10,20,.016,false)<20,"Smoothing bounded");
  var dimensions=new int[][]{{320,180},{480,300},{640,360},{854,480},{960,540},{1920,1080},{390,844}};
  for(var dim:dimensions)for(float requested:new float[]{.75f,1f,1.3f}){
   float s=HudMath.effectiveScale(dim[0],dim[1],requested);
   int cw=(int)(dim[0]/s), ch=(int)(dim[1]/s);
   for(var m:HudModule.values()){
    int x=HudMath.position(m.defaultX,cw,m.width), y=HudMath.position(m.defaultY,ch,m.height);
    ok(x>=0&&y>=0&&x+m.width<=cw&&y+m.height<=ch,"In bounds: "+m);
    ok(Math.abs(HudMath.normalized(x,cw,m.width)-m.defaultX)<.02,"Position round trip "+m);
    for(var n:HudModule.values())if(n.ordinal()>m.ordinal()){
     int nx=HudMath.position(n.defaultX,cw,n.width), ny=HudMath.position(n.defaultY,ch,n.height);
     boolean overlap=x<nx+n.width&&x+m.width>nx&&y<ny+n.height&&y+m.height>ny;
     ok(!overlap,"Default panels overlap at "+cw+"x"+ch+": "+m+" / "+n);
    }
   }
   var t=HudModule.TARGET; int tx=HudMath.position(t.defaultX,cw,t.width),ty=HudMath.position(t.defaultY,ch,t.height);
   ok(!new HudMath.Box(tx,ty,t.width,t.height).contains(cw/2.,ch/2.),"Target must not obscure crosshair");
  }
  var box=new HudMath.Box(10,20,30,40);ok(box.contains(10,20)&&!box.contains(40,60),"Hit testing edges");
  System.out.println("PASS: "+checks+" dependency-free geometry and motion assertions");
 }
}
