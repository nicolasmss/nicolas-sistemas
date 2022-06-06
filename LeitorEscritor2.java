/*
   Implementacao do Jantar dos Filosofos com Semaforo
   PUCRS - Escola Politecnica
   Prof: Fernando Dotti
*/
import java.util.concurrent.Semaphore;

// ============= Leitores e Escritores modelo do livro ==============
public class LeitorEscritor2 {
    Semaphore contagem;
    Semaphore porta;
    Semaphore turno;
    int contagemLeitor;

    public LeitorEscritor2(){
        contagem= new Semaphore(1);
        porta= new Semaphore(1);
        turno = new Semaphore(1);
        contagemLeitor = 0;
    }


    public static void main(String[] args) {
        LeitorEscritor2 leitorEscritor= new LeitorEscritor2();
        Leitor[] leitores = new Leitor[5];
        Escritor[] escritores = new Escritor[2];

        for (int i = 0; i < leitores.length; i++) {//leitores
            leitores[i] = new Leitor(i,leitorEscritor);
            System.out.println(leitores[i].id);
        }
        for (int i = 0; i < escritores.length; i++) {//escritores
            escritores[i] = new Escritor(i,leitorEscritor);
        }

        for (int i=0;i< leitores.length;i++){
            leitores[i].start();
        }
        for (int i=0;i< escritores.length;i++){
            escritores[i].start();
        }
    }
}

class Leitor extends Thread {

    LeitorEscritor2 leitorEscritor;
    int id;

    public Leitor(int _i, LeitorEscritor2 _leitorEscritor) {
        id = _i;
        leitorEscritor = _leitorEscritor;
    }

    public void run() {
        while (true) {
            try {
                leitorEscritor.turno.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            leitorEscritor.turno.release();
            try {
                leitorEscritor.contagem.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            leitorEscritor.contagemLeitor++;
            if (leitorEscritor.contagemLeitor==1){
                try {
                    leitorEscritor.porta.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.print("\nfirst"+id);
            }else{
                System.out.print("last"+id);
            }
            leitorEscritor.contagem.release();

            try {
                leitorEscritor.contagem.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            leitorEscritor.contagemLeitor--;
            if (leitorEscritor.contagemLeitor==0){
                leitorEscritor.porta.release();
            }
            leitorEscritor.contagem.release();
        }
    }
}
class Escritor extends Thread {

    private int i;
    //private Semaphore porta;
    private String espaco;
    int tipo;//1=escrito 2=leitor
    LeitorEscritor2 leitorEscritor;
    int id;

    public Escritor(int _i, LeitorEscritor2 _leitorEscritor) {
        i = _i;
        leitorEscritor = _leitorEscritor;
    }

    public void run() {
        while (true) {
            try {
                leitorEscritor.turno.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                leitorEscritor.porta.acquire();
            } catch (InterruptedException ie) {
            }
            System.out.print("\nescrevendo");
            leitorEscritor.turno.release();
            leitorEscritor.porta.release();
        }
    }
}

