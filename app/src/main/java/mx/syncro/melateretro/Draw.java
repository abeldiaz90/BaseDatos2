package mx.syncro.melateretro;
import java.util.*;
public class Draw {
 public final int contest; public final String date; public final int[] n; public final int additional; public final long jackpot;
 public Draw(int contest,String date,int[] n,int additional,long jackpot){this.contest=contest;this.date=date;this.n=n;this.additional=additional;this.jackpot=jackpot;Arrays.sort(this.n);}
 public String numbers(){return String.format(Locale.US,"%02d %02d %02d %02d %02d %02d",n[0],n[1],n[2],n[3],n[4],n[5]);}
}
