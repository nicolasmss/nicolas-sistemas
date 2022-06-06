/* 
   Implementacao do Jantar dos Filosofos com Semaforo
   PUCRS - Escola Politecnica
   Prof: Fernando Dotti
*/
import java.util.Scanner;
import java.util.concurrent.Semaphore;

// ============= Filosofo ==============
class Filosofo extends Thread {
    
	private int i;
	private Semaphore g1, g2;
	private String espaco;

    public Filosofo(int _i, Semaphore _g1, Semaphore _g2){		
		i = _i;   g1 = _g1;    g2 = _g2;	
		espaco = "  ";
		for (int k=0; k<i; k++){
			espaco = espaco + "                       ";
		}
    }

    public void run() {
       while (true) {
           //Scanner in = new Scanner(System.in);
           // pensa
   		   System.out.println(espaco+ i + ": Pensa ");
		   
		   // pega um garfo
		   try{g1.acquire();
		       }catch(InterruptedException ie){}
   		   System.out.println(espaco+ i + ": Pegou um ");
		   if (g2.tryAcquire()){
               // pega outro garfo

               System.out.println(espaco+ i + ": Pegou dois, come ");
               // come
               // solta garfos
               g1.release();
               g2.release();
           }else{
		       System.out.println(espaco+ i + ": solta ");
               g1.release();
           }
		   //in.nextLine();
      }
    }
}

class JantaFilosofos {	
	public static void main(String[] args) {
	    int FIL = 5;
	    Filosofo[] filosofos=new Filosofo[FIL];
		 
        Semaphore[] garfo = new Semaphore[FIL];
		for (int i=0; i< FIL; i++) {
		   garfo[i]= new Semaphore(1); 
	    }
        for (int i = 0; i < FIL; i++) {
			 	 filosofos[i] = new Filosofo(i,garfo[i],garfo[(i+1)%(FIL)]);
		}
        for (int i=0;i<FIL;i++){
        	filosofos[i].start();
		}
	}	  
}
	
