/* 
   Implementacao do Jantar dos Filosofos com Semaforo
   PUCRS - Escola Politecnica
   Prof: Fernando Dotti
*/
import java.util.concurrent.Semaphore;

// ============= Leitores e exritores meu modelo ==============
public class LeitorEscritor {
    Semaphore contagem;
    Semaphore porta;
    int contagemLeitor;
    Semaphore turno;

    public LeitorEscritor(){
        contagem= new Semaphore(1);
        porta= new Semaphore(1);
        turno = new Semaphore(1);
        contagemLeitor = 0;
    }


    public static void main(String[] args) {
        int qtdPessoas = 7;
        Pessoa[] pessoas = new Pessoa[qtdPessoas];
        LeitorEscritor leitorEscritor= new LeitorEscritor();

        for (int i = 0; i < 5; i++) {//leitores
            pessoas[i] = new Pessoa(i,2,leitorEscritor);
            pessoas[i].id=i;
        }
        for (int i = 5; i < qtdPessoas; i++) {//escritores
            pessoas[i] = new Pessoa(i,1,leitorEscritor);
        }

        for (int i=0;i<qtdPessoas;i++){
            pessoas[i].start();
        }
    }
}

class Pessoa extends Thread {

    private int i;
    //private Semaphore porta;
    private String espaco;
    int tipo;//1=escrito 2=leitor
    LeitorEscritor leitorEscritor;
    int id;

    public Pessoa(int _i, int _tipo, LeitorEscritor _leitorEscritor) {
        i = _i;
        //porta = _porta;
        tipo = _tipo;
        leitorEscritor = _leitorEscritor;

        espaco = "  ";
        for (int k = 0; k < i; k++) {
            espaco = espaco + "                       ";
        }
    }

    public void run() {
        while (true) {
            if (tipo == 2) {//leitor
                try {
                    leitorEscritor.turno.acquire();
                } catch (InterruptedException e) {}
                leitorEscritor.turno.release();
                try {
                    leitorEscritor.contagem.acquire();
                } catch (InterruptedException ie) {}
                if (leitorEscritor.contagemLeitor == 0) {
                    leitorEscritor.contagemLeitor++;
                    try {
                        leitorEscritor.porta.acquire();
                    } catch (InterruptedException ie) {}
                    System.out.print("\nfirst"+id);
                    leitorEscritor.contagem.release();

                } else {
                    System.out.print("last"+id);
                    leitorEscritor.contagemLeitor++;
                    leitorEscritor.contagem.release();
                }

                try {
                    leitorEscritor.contagem.acquire();
                } catch (InterruptedException ie) {}
                leitorEscritor.contagemLeitor--;
                if (leitorEscritor.contagemLeitor == 0) {
                    leitorEscritor.porta.release();
                }
                leitorEscritor.contagem.release();
            }
            else {//escritor
                try {
                    leitorEscritor.turno.acquire();
                } catch (InterruptedException e) {}
                try {
                    leitorEscritor.porta.acquire();
                } catch (InterruptedException ie) {}
                System.out.print("\nescrevendo");
                for (int i=0;i<50000;i++){

                }
                leitorEscritor.turno.release();
                leitorEscritor.porta.release();
            }
        }
    }
}
