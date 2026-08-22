package mx.syncro.melateretro;

import java.util.*;

public class Analyzer {
    public static class Stat { public final String key; public final double score; public final int count; Stat(String k,double s,int c){key=k;score=s;count=c;} }
    public static class Pick { public final int[] n; public final double score; public final String why; Pick(int[]n,double s,String w){this.n=n;score=s;why=w;} public String nums(){return String.format(Locale.US,"%02d %02d %02d %02d %02d %02d",n[0],n[1],n[2],n[3],n[4],n[5]);} }

    final List<Draw> ds;
    final int[] freq=new int[40],f30=new int[40],f100=new int[40],gap=new int[40];
    final Map<String,Integer> pairs=new HashMap<>(),trios=new HashMap<>();
    double meanSum=120,sdSum=25;

    public Analyzer(List<Draw>d){ds=d;calc();}
    String k2(int a,int b){return a+"-"+b;} String k3(int a,int b,int c){return a+"-"+b+"-"+c;}

    void calc(){
        Arrays.fill(gap,9999); ArrayList<Integer>sums=new ArrayList<>();
        for(int z=0;z<ds.size();z++){
            Draw d=ds.get(z); int sm=0;
            for(int x:d.n){freq[x]++;if(z<30)f30[x]++;if(z<100)f100[x]++;if(gap[x]==9999)gap[x]=z;sm+=x;}
            sums.add(sm);
            for(int i=0;i<6;i++)for(int j=i+1;j<6;j++)pairs.merge(k2(d.n[i],d.n[j]),1,Integer::sum);
            for(int i=0;i<6;i++)for(int j=i+1;j<6;j++)for(int k=j+1;k<6;k++)trios.merge(k3(d.n[i],d.n[j],d.n[k]),1,Integer::sum);
        }
        if(!sums.isEmpty()){meanSum=0;for(int x:sums)meanSum+=x;meanSum/=sums.size();double v=0;for(int x:sums)v+=(x-meanSum)*(x-meanSum);sdSum=Math.sqrt(v/sums.size());}
    }
    double norm(int c,int draws){return draws==0?0:(c/(double)draws)/(6.0/39.0);}
    public List<Stat>numbers(){ArrayList<Stat>o=new ArrayList<>();for(int n=1;n<=39;n++){double s=.45*norm(freq[n],ds.size())+.35*norm(f100[n],Math.min(100,ds.size()))+.20*norm(f30[n],Math.min(30,ds.size()));o.add(new Stat(String.format(Locale.US,"%02d",n),s,freq[n]));}o.sort((a,b)->Double.compare(b.score,a.score));return o;}
    List<Stat>top(Map<String,Integer>m){ArrayList<Stat>o=new ArrayList<>();for(Map.Entry<String,Integer>e:m.entrySet())o.add(new Stat(e.getKey(),e.getValue(),e.getValue()));o.sort((a,b)->Integer.compare(b.count,a.count));return o;}
    public List<Stat>topPairs(){return top(pairs);} public List<Stat>topTrios(){return top(trios);}

    double score(int[]x){
        double s=0;
        for(int n:x)s+=.36*norm(freq[n],ds.size())+.29*norm(f100[n],Math.min(100,ds.size()))+.20*norm(f30[n],Math.min(30,ds.size()))+.15*Math.min(gap[n],35)/35.0;
        int pc=0,tc=0;for(int i=0;i<6;i++)for(int j=i+1;j<6;j++)pc+=pairs.getOrDefault(k2(x[i],x[j]),0);for(int i=0;i<6;i++)for(int j=i+1;j<6;j++)for(int k=j+1;k<6;k++)tc+=trios.getOrDefault(k3(x[i],x[j],x[k]),0);s+=pc*.05+tc*.06;
        int ev=0,sum=0;for(int n:x){if(n%2==0)ev++;sum+=n;}if(ev==3)s+=.90;else if(ev==2||ev==4)s+=.45;else s-=.45;
        s-=Math.abs(sum-meanSum)/Math.max(10,sdSum)*.22;
        int span=x[5]-x[0];if(span>=24&&span<=33)s+=.35;else if(span<18)s-=.20;
        int cons=0;for(int i=1;i<x.length;i++)if(x[i]==x[i-1]+1)cons++;if(cons>2)s-=.35;
        int ov=recentOverlap(x);if(ov>=5)s-=1.8;else if(ov==4)s-=.9;
        return s;
    }
    int recentOverlap(int[]x){int mx=0;for(int z=0;z<Math.min(20,ds.size());z++){int c=0;for(int a:x)for(int b:ds.get(z).n)if(a==b)c++;mx=Math.max(mx,c);}return mx;}
    String why(int[]x){int sum=0,ev=0,pc=0,tc=0;for(int n:x){sum+=n;if(n%2==0)ev++;}for(int i=0;i<6;i++)for(int j=i+1;j<6;j++)pc+=pairs.getOrDefault(k2(x[i],x[j]),0);for(int i=0;i<6;i++)for(int j=i+1;j<6;j++)for(int k=j+1;k<6;k++)tc+=trios.getOrDefault(k3(x[i],x[j],x[k]),0);return "frecuencia+recencia · par/impar "+ev+"/"+(6-ev)+" · suma "+sum+" · rango "+(x[5]-x[0])+" · pares "+pc+" · tríos "+tc;}

    public List<Pick>generate(int limit){
        int desired=Math.max(10,limit); PriorityQueue<Pick>pq=new PriorityQueue<>((a,b)->Double.compare(a.score,b.score)); Random r=new Random(3901661L); HashSet<String>seen=new HashSet<>();
        for(int t=0;t<300000;t++){
            TreeSet<Integer>set=new TreeSet<>();while(set.size()<6)set.add(1+r.nextInt(39));int[]x=new int[6];int q=0;for(int n:set)x[q++]=n;String key=Arrays.toString(x);if(!seen.add(key))continue;Pick p=new Pick(x,score(x),why(x));if(pq.size()<180)pq.add(p);else if(p.score>pq.peek().score){pq.poll();pq.add(p);}
        }
        ArrayList<Pick>all=new ArrayList<>(pq);all.sort((a,b)->Double.compare(b.score,a.score));ArrayList<Pick>out=new ArrayList<>();
        for(Pick p:all){boolean ok=true;for(Pick q:out)if(overlap(p.n,q.n)>=5){ok=false;break;}if(ok)out.add(p);if(out.size()>=desired)break;}
        if(out.size()<desired)for(Pick p:all){boolean exists=false;for(Pick q:out)if(Arrays.equals(p.n,q.n)){exists=true;break;}if(!exists)out.add(p);if(out.size()>=desired)break;}
        return out;
    }
    int overlap(int[]a,int[]b){int c=0;for(int x:a)for(int y:b)if(x==y)c++;return c;}
    public double meanSum(){return meanSum;}
}
