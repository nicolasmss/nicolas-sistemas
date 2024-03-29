// PUCRS - Escola Politécnica - Sistemas Operacionais
// Prof. Fernando Dotti
// Código fornecido como parte da solução do projeto de Sistemas Operacionais
//
// Fase 1 - máquina virtual (vide enunciado correspondente)
//

import javax.xml.crypto.Data;
import java.util.*;
import java.util.concurrent.Semaphore;

public class Sistema {
	
	// -------------------------------------------------------------------------------------------------------
	// --------------------- H A R D W A R E - definicoes de HW ---------------------------------------------- 

	// -------------------------------------------------------------------------------------------------------
	// --------------------- M E M O R I A -  definicoes de opcode e palavra de memoria ---------------------- 
	
	public class Word { 	// cada posicao da memoria tem uma instrucao (ou um dado)
		public Opcode opc; 	//
		public int r1; 		// indice do primeiro registrador da operacao (Rs ou Rd cfe opcode na tabela)
		public int r2; 		// indice do segundo registrador da operacao (Rc ou Rs cfe operacao)
		public int p; 		// parametro para instrucao (k ou A cfe operacao), ou o dado, se opcode = DADO

		public Word(Opcode _opc, int _r1, int _r2, int _p) {  
			opc = _opc;   r1 = _r1;    r2 = _r2;	p = _p;
		}
	}
    // -------------------------------------------------------------------------------------------------------

	// -------------------------------------------------------------------------------------------------------
    // --------------------- C P U  -  definicoes da CPU ----------------------------------------------------- 

	public enum Opcode {
		DATA, ___,		    // se memoria nesta posicao tem um dado, usa DATA, se nao usada ee NULO ___
		JMP, JMPI, JMPIG, JMPIL, JMPIE, JMPIM, JMPIGM, JMPILM, JMPIEM, STOP,   // desvios e parada
		ADDI, SUBI,  ADD, SUB, MULT,         // matematicos
		LDI, LDD, STD,LDX, STX, SWAP,       // movimentacao
        TRAP;
	}

	public class CPU extends Thread {
							// característica do processador: contexto da CPU ...
		private int pc; 			// ... composto de program counter,
		private Word ir; 			// instruction register,
		private int[] reg;       	// registradores da CPU
        //private int tipoInterrupcao=-1; //0-endereco invalido | 1-instrucao invalida | 2-overflow | 3-final de programa
        //int numero;
        public int[] tabAtual;      //frames do processo que esta executando
        public int cont = 0;
        public GerenciadorProcesso.PCB processo;



		private Word[] m;   // CPU acessa MEMORIA, guarda referencia 'm' a ela. memoria nao muda. ee sempre a mesma.
			
		public CPU(Word[] _m) {     // ref a MEMORIA e interrupt handler passada na criacao da CPU
			m = _m; 				// usa o atributo 'm' para acessar a memoria.
			reg = new int[10]; 		// aloca o espaço dos registradores
		}

		public void setContext(int _pc, int[] _reg) {       // no futuro esta funcao vai ter que ser
			pc = _pc;
			reg = _reg;
		}
	
		private void dump(Word w) {
			System.out.print("[ "); 
			System.out.print(w.opc); System.out.print(", ");
			System.out.print(w.r1);  System.out.print(", ");
			System.out.print(w.r2);  System.out.print(", ");
			System.out.print(w.p);  System.out.println("  ] ");
		}

        private void showState(){
			 System.out.println("       "+ pc); 
			   System.out.print("           ");
			 for (int i=0; i<8; i++) { System.out.print("r"+i);   System.out.print(": "+reg[i]+"     "); };  
			 System.out.println("");
			 System.out.print("           ");  dump(ir);
		}

		public void run() { 		// execucao da CPU supoe que o contexto da CPU, vide acima, esta devidamente setado
			int numero;
		    while (true) { 			// ciclo de instrucoes. acaba cfe instrucao, veja cada caso.
				// FETCH
                if (cont == 0){
                    try {
                        teste2.acquire();
                    } catch (InterruptedException e) {}
                }
                if (cont==escalonador.delta){
                    cont=0;
                    escalonador.escalona(processo,pc,reg);
                    teste3.release();
                    //break; //nunca para
                    try {
                        teste2.acquire();
                    } catch (InterruptedException e) {}
                }
                cont++;
                ir = m[gerenciadorMemoria.tradutor(pc,tabAtual)]; 	// busca posicao da memoria apontada por pc, guarda em ir
                //showState();
				// EXECUTA INSTRUCAO NO ir
					switch (ir.opc) { // para cada opcode, sua execução
                        //0-endereco invalido | 1-instrucao invalida | 2-overflow | 3-final de programa
						case LDI: // Rd ← k
                            if (verificaInterrupcaoInstrucao(ir.r1)>=0){
                                break;
                            }
                            if (verificaInterrupcaoOverflow(ir.r1)>=0){
                                break;
                            }
							reg[ir.r1] = ir.p;
							pc++;
							break;

						case STD: // [A] ← Rs
                            if (verificaInterrupcaoEndereco(gerenciadorMemoria.tradutor(ir.p,tabAtual))>=0){
                                break;
                            }
                            m[gerenciadorMemoria.tradutor(ir.p,tabAtual)].opc = Opcode.DATA;
                            m[gerenciadorMemoria.tradutor(ir.p,tabAtual)].p = reg[ir.r1];
                            pc++;
                            break;

						case LDD: // Rd ← [A]
                            if (verificaInterrupcaoEndereco(gerenciadorMemoria.tradutor(ir.p,tabAtual))>=0){
                                break;
                            }
							reg[ir.r1] = m[gerenciadorMemoria.tradutor(ir.p,tabAtual)].p;
							pc++;
							break;

						case LDX: // Rd ← [Rs]
                            if (verificaInterrupcaoEndereco(gerenciadorMemoria.tradutor(reg[ir.r2],tabAtual))>=0){
                                break;
                            }
							reg[ir.r1] = m[gerenciadorMemoria.tradutor(reg[ir.r2],tabAtual)].p;
							pc++;
							break;

						case SWAP: // T = Ra | Ra = Rb | Rb = T
							int t = reg[ir.r1];
							reg[ir.r1] = reg[ir.r2];
							reg[ir.r2] = t;
							pc++;
							break;

						case ADD: // Rd ← Rd + Rs
                            numero = reg[ir.r1] + reg[ir.r2];
                            if (verificaInterrupcaoOverflow(numero)>=0){
                                break;
                            }
							reg[ir.r1] = reg[ir.r1] + reg[ir.r2];
							pc++;
							break;

						case MULT: // Rd ← Rd * Rs
                            numero = reg[ir.r1] * reg[ir.r2];
                            if (verificaInterrupcaoOverflow(numero)>=0){
                                break;
                            }
							reg[ir.r1] = reg[ir.r1] * reg[ir.r2];
							// gera um overflow
							// -->  LIGA INT  (1)
							pc++;
							break;

						case ADDI: // Rd ← Rd + k
                            numero = reg[ir.r1] + ir.p;
                            if (verificaInterrupcaoOverflow(numero)>=0){
                                break;
                            }
							reg[ir.r1] = reg[ir.r1] + ir.p;
							pc++;
							break;

						case STX: // [Rd] ←Rs
                            if (verificaInterrupcaoEndereco(gerenciadorMemoria.tradutor(reg[ir.r1],tabAtual))>=0){
                                break;
                            }
							    m[gerenciadorMemoria.tradutor(reg[ir.r1],tabAtual)].opc = Opcode.DATA;
							    m[gerenciadorMemoria.tradutor(reg[ir.r1],tabAtual)].p = reg[ir.r2];
								pc++;
							break;

						case SUB: // Rd ← Rd - Rs
                            numero = reg[ir.r1] - reg[ir.r2];
                            if (verificaInterrupcaoOverflow(numero)>=0){
                                break;
                            }
							reg[ir.r1] = reg[ir.r1] - reg[ir.r2];
							pc++;
							break;

						case SUBI: // Rd ← Rd - K
                            numero = reg[ir.r1] - ir.p;
                            if (verificaInterrupcaoOverflow(numero)>=0){
                                break;
                            }
							reg[ir.r1] = reg[ir.r1] - ir.p;
							pc++;
							break;

						case JMP: //  PC ← k
                            if (verificaInterrupcaoEndereco(gerenciadorMemoria.tradutor(ir.p,tabAtual))>=0){
                                break;
                            }
                            pc = ir.p;
						    break;

						case JMPI: //  PC ← Rs
                            if (verificaInterrupcaoEndereco(gerenciadorMemoria.tradutor(reg[ir.r1],tabAtual))>=0){
                                break;
                            }
							pc = reg[ir.r1];
							break;

						case JMPIG: // If Rc > 0 Then PC ← Rs Else PC ← PC +1
                            if (verificaInterrupcaoEndereco(gerenciadorMemoria.tradutor(reg[ir.r1],tabAtual))>=0){
                                break;
                            }
							if (reg[ir.r2] > 0) {
								pc = reg[ir.r1];
							} else {
								pc++;
							}
							break;

                        case JMPIL: // If Rc < 0 Then PC ← Rs Else PC ← PC +1
                            if (verificaInterrupcaoEndereco(gerenciadorMemoria.tradutor(reg[ir.r1],tabAtual))>=0){
                                break;
                            }
                            if (reg[ir.r2] < 0) {
                                pc = reg[ir.r1];
                            } else {
                                pc++;
                            }
                            break;

						case JMPIE: // If Rc = 0 Then PC ← Rs Else PC ← PC +1
                            if (verificaInterrupcaoEndereco(gerenciadorMemoria.tradutor(reg[ir.r1],tabAtual))>=0){
                                break;
                            }
							if (reg[ir.r2] == 0) {
								pc = reg[ir.r1];
							} else {
								pc++;
							}
							break;

						case JMPIM: // PC = [A]
                            if (verificaInterrupcaoEndereco(gerenciadorMemoria.tradutor(reg[ir.p],tabAtual))>=0){
                                break;
                            }
							pc = m[gerenciadorMemoria.tradutor(reg[ir.p],tabAtual)].p;
							break;

						case JMPIGM: // If Rc > 0 Then PC ← [A] Else PC ← PC +1
                            if (verificaInterrupcaoEndereco(gerenciadorMemoria.tradutor(ir.p,tabAtual))>=0){
                                break;
                            }
							if (reg[ir.r2] > 0) {
								pc = m[gerenciadorMemoria.tradutor(ir.p,tabAtual)].p;
							} else {
								pc++;
							}
							break;

						case JMPILM: // If Rc < 0 Then PC ← [A] Else PC ← PC +1
                            if (verificaInterrupcaoEndereco(gerenciadorMemoria.tradutor(ir.p,tabAtual))>=0){
                                break;
                            }
							if (reg[ir.r2] < 0) {
								pc = m[gerenciadorMemoria.tradutor(ir.p,tabAtual)].p;
							} else {
								pc++;
							}
							break;

						case JMPIEM: // If Rc = 0 Then PC ← [A] Else PC ← PC +1
                            if (verificaInterrupcaoEndereco(gerenciadorMemoria.tradutor(ir.p,tabAtual))>=0){
                                break;
                            }
							if (reg[ir.r2] == 0) {
								pc = m[gerenciadorMemoria.tradutor(ir.p,tabAtual)].p;
							} else {
								pc++;
							}
							break;

						case STOP: // por enquanto, para execucao
                            if (verificaInterrupcaoFinal(Opcode.STOP)>=0){
                                break;
                            }
							break;

                        case TRAP:
                            tipoInterrupcao = 4;
                            //funcaoTRAP();
                            pc++;
                            break;

						default:
						    // opcode desconhecido
							// liga interrup (2)
                            if (verificaInterrupcaoInstrucao(-1)>=0){
                                break;
                            } //instrução invalida
					}
				
				// VERIFICA INTERRUPÇÃO !!! - TERCEIRA FASE DO CICLO DE INSTRUÇÕES
				if (ir.opc==Opcode.STOP) {
					tipoInterrupcao = 3;
					processo.tipoInterrupcao = tipoInterrupcao;
					tipoInterrupcao= -1;
                    escalonador.atualizaProcesso(processo,pc,reg,processo.tipoInterrupcao);
                    chamadas.filaDesaloca.add(processo.id);
                    //tratamentoInterrupcao();
                    //desaloca(vm.cpu.processo.id);//desaloca no fim do processo
					cont = 0;
                    semChamada.release();
                    ehDesa.release();
					teste3.release();

					//break; // break sai do loop da cpu // nunca mais sai

			    // if int ligada - vai para tratamento da int 
				//     desviar para rotina java que trata int

				}else if(tipoInterrupcao==4){
                    processo.tipoInterrupcao = tipoInterrupcao;
                    tipoInterrupcao= -1;
                    escalonador.atualizaProcesso(processo,pc,reg,processo.tipoInterrupcao);
                    escalonador.bloqueados.add(processo.id);
				    //tratamentoInterrupcao();
                    cont = 0;
                    teste3.release();
                    semaforoTrap.release();
					//break;
				}else if(tipoInterrupcao>=0){
                    processo.tipoInterrupcao = tipoInterrupcao;
                    tipoInterrupcao= -1;
                    escalonador.atualizaProcesso(processo,pc,reg,processo.tipoInterrupcao);
                    chamadas.filaDesaloca.add(processo.id);
                    //tratamentoInterrupcao();
                    cont = 0;
                    teste3.release();
                    semChamada.release();
                    ehDesa.release();
                    //break;
                }
			}
		}
	}
    // ------------------ C P U - fim ------------------------------------------------------------------------
	// -------------------------------------------------------------------------------------------------------

	
    // ------------------- V M  - constituida de CPU e MEMORIA -----------------------------------------------
    // -------------------------- atributos e construcao da VM -----------------------------------------------
	public class VM {
		public int tamMem;    
        public Word[] m;     
        public CPU cpu;


        public VM(){
	     // memória
  		 	 tamMem = 1024;
			 m = new Word[tamMem]; // m ee a memoria
			 for (int i=0; i<tamMem; i++) {
			     m[i] = new Word(Opcode.___,-1,-1,-1);
			 };
	  	 // cpu
			 cpu = new CPU(m);   // cpu acessa memória
	    }





	}
    // ------------------- V M  - fim ------------------------------------------------------------------------
	// -------------------------------------------------------------------------------------------------------

    // --------------------H A R D W A R E - fim -------------------------------------------------------------
    // -------------------------------------------------------------------------------------------------------

	// -------------------------------------------------------------------------------------------------------
	// -------------------------------------------------------------------------------------------------------
	// ------------------- S O F T W A R E - inicio ----------------------------------------------------------

		// -------------------------------------------  funcoes de um monitor
	public class Monitor {
			public void dump(Word w) {
				System.out.print("[ "); 
				System.out.print(w.opc); System.out.print(", ");
				System.out.print(w.r1);  System.out.print(", ");
				System.out.print(w.r2);  System.out.print(", ");
				System.out.print(w.p);  System.out.println("  ] ");
			}
			public void dump(Word[] m, int ini, int fim) {
				for (int i = ini; i < fim; i++) {
					System.out.print(i); System.out.print(":  ");  dump(m[i]);
				}
			}

            public void executa(int _pc,int[] _reg) {
                vm.cpu.setContext(_pc,_reg); // monitor seta contexto - pc aponta para inicio do programa
                teste2.release();
                //vm.cpu.run();                  //                         e cpu executa
                //tratamentoInterrupcao();               // note aqui que o monitor espera que o programa carregado acabe normalmente
            }


		}
	   // -------------------------------------------  
		



	// -------------------------------------------------------------------------------------------------------
    // -------------------  S I S T E M A --------------------------------------------------------------------

	public VM vm;
	public Monitor monitor;
	public static Programas progs;
	public int tipoInterrupcao=-1;
	public GerenciadorMemoria gerenciadorMemoria;
	public GerenciadorProcesso gerenciadorProcesso;
	public Escalonador escalonador;
	public Console console;
	public Chamadas chamadas;
	public int sequencial = 0;


    public Sistema(){   // a VM com tratamento de interrupções
		 vm = new VM();
		 monitor = new Monitor();
		 progs = new Programas();
		 gerenciadorMemoria = new GerenciadorMemoria(vm.m);
		 gerenciadorProcesso = new GerenciadorProcesso(gerenciadorMemoria);
		 escalonador = new Escalonador();
		 console = new Console();
		 chamadas =new Chamadas();
	}

    public void cria(Word[] programa){
        if (!gerenciadorProcesso.criaProcesso(programa)){
            System.out.println("erro ao criar processo");
        }
        System.out.println("---------------------------------- programa carregado ");
        GerenciadorProcesso.PCB pcb = gerenciadorProcesso.listaProcesso.get(gerenciadorProcesso.listaProcesso.size()-1);

        System.out.println("id do programa: "+pcb.id);
        System.out.println("paginas alocadas pelo programa: "+pcb.paginasString()+"\n");
        if (auto){
            teste3.release(); //automatizar ao criar
        }
    }

    public void dump(int id){
        GerenciadorProcesso.PCB pcb = gerenciadorProcesso.findPCBById(id);
        if (pcb==null){
            System.out.println("não pode usar o dump\nprocesso vazio ou não encontrado\n");
        }else{
            System.out.println("processo id = "+id);
            for (int i = 0;i<pcb.paginasDoProcesso.length;i++){
                monitor.dump(vm.m,pcb.paginasDoProcesso[i]*gerenciadorMemoria.tamFrame,(pcb.paginasDoProcesso[i]+1)* gerenciadorMemoria.tamFrame);
            }
            System.out.println();
        }
    }

    public void dumpm(int inicio, int fim){
        System.out.println("dump de "+inicio+" até "+fim);
        monitor.dump(vm.m,inicio,fim);
        System.out.println();
    }

    public void desaloca(int id){
        if (gerenciadorProcesso.desalocaProcesso(id)){
            System.out.println("processo desalocado id = "+id+"\n");
        }else{
            System.out.println("erro ao desalocar\n");
        }
    }

    public void resetaReg(){
        Arrays.fill(vm.cpu.reg, 0);
    }

    public void tratamentoInterrupcao(int interrupcao)	{
        switch (interrupcao){
            case 0:
                System.out.println("Endereco invalido");
                break;
            case 1:
                System.out.println("Instrucao invalida");
                break;
            case 2:
                System.out.println("Overflow");
                break;
            case 3:
                System.out.println("Final de programa");
                break;
            case 4:
                System.out.println("Trap");
                break;
        }
        tipoInterrupcao = -1;
    }

    public int verificaInterrupcaoEndereco(int endereco){
        if(endereco<0||endereco>1023){
            tipoInterrupcao = 0;
            return 0;// 0 = endereco invalido
        }
        return -1;
    }

    public int verificaInterrupcaoInstrucao(int instrucao){
        if(instrucao<0||instrucao>9){
            tipoInterrupcao = 1;
            return 1; // 1 = instrução invalida
        }
        return -1;
    }

    public int verificaInterrupcaoOverflow(int numero){
        if(numero>1000000000||numero<-1000000000){
            tipoInterrupcao = 2;
            return 2;// 2 = overflow
        }
        return -1;
    }

    public int verificaInterrupcaoFinal(Opcode isStop){
        if(isStop.equals(Opcode.STOP)){
            tipoInterrupcao = 3;
            return 3;// 3 = final de programa
        }
        return -1;
    }

    public void funcaoTRAP(){
        Scanner in = new Scanner(System.in);
        if (vm.cpu.reg[8]==1){
            vm.cpu.m[gerenciadorMemoria.tradutor(vm.cpu.reg[9],vm.cpu.tabAtual)].p = in.nextInt();
        }
        if (vm.cpu.reg[8]==2){
            System.out.println("out: " + vm.cpu.m[gerenciadorMemoria.tradutor(vm.cpu.reg[9],vm.cpu.tabAtual)].p);
        }
        vm.cpu.pc++;
    }

    public void funcaoTRAP(GerenciadorProcesso.PCB processo){
        Scanner in = new Scanner(System.in);
        if (processo.registadores[8]==1){
            System.out.println("escrever valor TRAP:");
            vm.cpu.m[gerenciadorMemoria.tradutor(processo.registadores[9],processo.paginasDoProcesso)].p = in.nextInt();
        }
        if (processo.registadores[8]==2){
            System.out.println("out: " + vm.cpu.m[gerenciadorMemoria.tradutor(processo.registadores[9],processo.paginasDoProcesso)].p);
        }
    }

    public class GerenciadorMemoria{
        public int tamPag = 16;
        public int tamFrame = tamPag;
        public int nroFrames = vm.tamMem/tamPag;
        public int tamMemoria;
        public Word[] mem;
        public boolean[] frameLivre = new boolean[nroFrames];
        public int qtdFramesLivre = nroFrames;//numero de frames livres restantes
        public ArrayList<Integer> framesUsados = new ArrayList<Integer>();//tds os framesque são usados
        public int qtdProg = 0;//contador de programas alocados
        public boolean valida = true;

        public GerenciadorMemoria(Word[] mem){
            this.mem = mem;
            tamMemoria = mem.length;
            Arrays.fill(frameLivre, true);
            frameLivre[1]=false;
            frameLivre[3]=false;
            qtdFramesLivre-=2;
        }

        public ArrayList<Integer> aloca(int nroPalavras, Word[] prog){//
            nroPalavras = nroPalavras/tamFrame;//recebe o tamanho do prog e transforma no tamanho de paginas de prog
            if (qtdFramesLivre<nroPalavras){//programa muito grande pra quantidade livre no momento
                valida =false;
                return framesUsados;
            }
            qtdFramesLivre = qtdFramesLivre - nroPalavras;
            int contadorPalavra = 0;
            int contProg = 0;
            for (int i=0;i<nroFrames;i++){
                if(contadorPalavra>nroPalavras){
                    break;
                }
                if(frameLivre[i]){
                    frameLivre[i] = false;
                    framesUsados.add(i);
                    contadorPalavra++;
					for(int j=tamFrame*i;j<tamFrame*(i+1);j++){
						if (contProg>=prog.length){
							break;
						}
						vm.cpu.m[j]= prog[contProg];
						contProg++;
					}
                }
            }
            qtdProg++;
            return framesUsados;
        }
        // retorna true se consegue alocar ou falso caso negativo
        // cada posição i do vetor de saída “tabelaPaginas” informa em que frame a página i deve ser hospedada

        public void desaloca(int[] tabela){//DESALOCAGM
        	boolean apagou = false;
			for (int i=0;i< tabela.length;i++){
			    if (frameLivre[tabela[i]]){
			        continue;
                }
			    framesUsados.remove((Object) tabela[i]);
                for(int j=tamFrame*tabela[i];j<tamFrame*(tabela[i]+1);j++){
                    vm.cpu.m[j]= new Word(Opcode.___, -1, -1, -1);
                }
                frameLivre[tabela[i]] = true;
                qtdFramesLivre++;
			    apagou = true;
			}
			if (apagou){
				qtdProg--;
			}
        }
        // simplesmente libera os frames alocados

        public int[] retornaUltimos(int numero){
            int[] vet = new int[numero];
            int cont=0;
            for (int i=framesUsados.size()-numero;i<framesUsados.size();i++){
                vet[cont] = framesUsados.get(i);
                cont++;
            }
            return vet;
        }

        public int getQtdProg() {
            return qtdProg;
        }

        public int tradutor(int endereco, int [] vetPaginas){
            if(endereco/tamFrame<0||endereco/tamFrame>=nroFrames){
                return -1;
            }
            return (vetPaginas[endereco/tamFrame]*tamFrame)+(endereco%tamFrame);
            //T(A) = ( tabPaginas [(A div tamPag)] * tamFrame ) + ( A mod tamPag )
        }
    }

    public class GerenciadorProcesso{
        GerenciadorMemoria gm;
        ArrayList<PCB> listaProcesso = new ArrayList<PCB>();
        int contadorId = 0;

        public GerenciadorProcesso(GerenciadorMemoria gm){
            this.gm = gm;
        }

        public boolean criaProcesso(Word[] prog){
            ArrayList<Integer> framesAlocados = gm.aloca(prog.length,prog);
            if (!gm.valida){
                return false;
            }
            int tamProg = (prog.length/gm.tamFrame)+1;
            int[] pagProcesso = gm.retornaUltimos(tamProg);
            PCB processo = new PCB(contadorId,pagProcesso);
            escalonador.filaReady.add(contadorId);//FILA
            contadorId++;
            listaProcesso.add(processo);
            return true;
        }

        public void listaId(){
            String lol = "";
            for (int i=0;i<listaProcesso.size();i++){
                lol = lol + listaProcesso.get(i).id + ", ";
            }
            System.out.println(lol);
        }

        public PCB findPCBById(int id){
            for (PCB pcb : listaProcesso) {
                if (pcb.id == id) {
                    return pcb;
                }
            }
            return null;
        }

        public boolean desalocaProcesso(int id){
            PCB pcb = findPCBById(id);
            if (pcb==null){
                return false;
            }
            gm.desaloca(pcb.paginasDoProcesso);
            listaProcesso.remove(pcb);
            return true;
        }

        public class PCB{
            public int id;
            public int[] paginasDoProcesso;
            public int[] registadores = new int[10];
            public int pc = 0;
            public int tipoInterrupcao = -1;

            public PCB(int id, int[] paginasDoProcesso){
                this.id = id;
                this.paginasDoProcesso = paginasDoProcesso;
                Arrays.fill(registadores,0);
            }

            public String paginasString(){
                String lol ="";
                for (int i =0;i<paginasDoProcesso.length;i++){
                    lol = lol + paginasDoProcesso[i]+", ";
                }
                return lol;
            }

            public void atualizaPc(int novo){pc = novo;}

            public void atualizaRegistradores(int[] novo){
                registadores = novo;
            }

            public void atualizaTipoInterrupcao(int novo){tipoInterrupcao = novo;}
        }
    }

    Semaphore teste = new Semaphore(0);//testando inutil
    Semaphore teste2 = new Semaphore(0);//testando cpu
    Semaphore teste3 = new Semaphore(0);//testando escalonador
    Semaphore semaforoTrap = new Semaphore(0);//testando
    Semaphore semanforoMenu = new Semaphore(1);//testando
    Semaphore semanFila = new Semaphore(1);//testando
    public class Escalonador extends Thread{
        public int delta = 10;
        public List<Integer> filaReady = new ArrayList<>();
        public List<Integer> bloqueados = new ArrayList<>();


        public void escalona(GerenciadorProcesso.PCB processo,
                                  int pc, int[] reg){
            processo.atualizaPc(pc);
            processo.atualizaRegistradores(reg);
            try {
                semanFila.acquire();
            } catch (InterruptedException e) {}
            filaReady.add(processo.id);
            semanFila.release();
            //teste.release();
        }

        public void atualizaProcesso(GerenciadorProcesso.PCB processo,
                             int pc, int[] reg, int interrupcao){
            processo.atualizaPc(pc);
            processo.atualizaRegistradores(reg);
            processo.atualizaTipoInterrupcao(interrupcao);

        }

        boolean listavazia = false;
        public void run() {
            while(true){
                try {
                    teste3.acquire();
                } catch (InterruptedException e) {}
                //System.out.println(filaReady.isEmpty());

                if (filaReady.isEmpty()){
                    System.out.println("fila vazia");
                    listavazia=true;
                }else{
                    listavazia=false;
                    GerenciadorProcesso.PCB processo = gerenciadorProcesso
                            .findPCBById(filaReady.get(0));//running
                    if (processo!=null){
                        vm.cpu.tabAtual = processo.paginasDoProcesso;
                        System.out.println("processo id = "+ processo.id +" executando");
                        filaReady.remove(0);
                        vm.cpu.processo = processo;
                        monitor.executa(processo.pc, processo.registadores);
                        //System.out.println("---------------------------------- programa executado\n");

                    }
                }
            }
        }
    }

    public boolean esperaTrap = false;
    public class Console extends Thread{

        public void run(){
            while(true){
                try {
                    semaforoTrap.acquire();
                } catch (InterruptedException e) {}

                GerenciadorProcesso.PCB processo = gerenciadorProcesso
                        .findPCBById(escalonador.bloqueados.get(0));

                tratamentoInterrupcao(processo.tipoInterrupcao);

                if (processo.tipoInterrupcao==4){
                    esperaTrap=true;
                    try {
                        semanforoMenu.acquire();
                    } catch (InterruptedException e) {}

                    esperaTrap=false;
                    funcaoTRAP(processo);
                    processo.tipoInterrupcao =-1;
                    try {
                        semanFila.acquire();
                    } catch (InterruptedException e) {}
                    escalonador.filaReady.add(processo.id);
                    semanFila.release();
                    escalonador.bloqueados.remove(0);
                    semanforoMenu.release();
                    if (escalonador.listavazia){
                        teste3.release();
                    }
                }
            }
        }
    }

    Semaphore semChamada = new Semaphore(0);
    Semaphore ehDesa = new Semaphore(0);
    Semaphore ehCria = new Semaphore(0);


    public class Chamadas extends Thread{
        List<Integer> filaDesaloca = new ArrayList<Integer>();
        List<Word[]> filaCria = new ArrayList<Word[]>();


        public void run() {
            while(true) {
                try {
                    semChamada.acquire();
                } catch (InterruptedException e) {}

                if (ehCria.tryAcquire()){
                    cria(filaCria.get(0));
                    filaCria.remove(0);
                }

                if (ehDesa.tryAcquire()){
                    GerenciadorProcesso.PCB processo = gerenciadorProcesso
                            .findPCBById(filaDesaloca.get(0));

                    tratamentoInterrupcao(processo.tipoInterrupcao);
                    if (processo.tipoInterrupcao==3) {
                        processo.tipoInterrupcao = -1;
                        try {
                            semanFila.acquire();
                        } catch (InterruptedException e) {}
                        desaloca(processo.id);
                        semanFila.release();
                        filaDesaloca.remove(0);
                    }
                }
            }
        }
    }



    // -------------------  S I S T E M A - fim --------------------------------------------------------------
    // -------------------------------------------------------------------------------------------------------

    // -------------------------------------------------------------------------------------------------------
    // ------------------- instancia e testa sistema
	public static void main(String args[]) {
		Sistema s = new Sistema();
        s.vm.cpu.start();
        s.escalonador.start();
        s.console.start();
        s.chamadas.start();
        s.menu2();

    }

    boolean auto = true;
    public void menu2(){
        Scanner in = new Scanner(System.in);
        String menu = "MENU\n" +
                "1 - cria processo\n" +
                "2 - parar de executar automaticamente ao criar processos\n" +
                "3 - começar a executar manualmente\n" +
                "está automatico = "+auto+"\n" +
                "0 - pular";

       String programas = "Programas disponiveis:\n" +
                "1 - PA (fibonacci)\n" +
                "2 - PB (fatorial)\n" +
                "3 - PC (bubble sort)\n" +
                "4 - Fibonacci TRAP_IN\n" +
                "5 - Fatorial TRAP_OUT";
        while(true){
            try {
                semanforoMenu.acquire();
            } catch (InterruptedException e) {}
            System.out.println(menu);
            switch (in.nextInt()){
                case 1:
                    System.out.println("escolha um programa para criar");
                    System.out.println(programas);
                    switch (in.nextInt()){
                        case 1:
                            //cria(progs.PA);
                            chamadas.filaCria.add(progs.PA);
                            ehCria.release();
                            semChamada.release();
                            break;
                        case 2:
                            //cria(progs.PB);
                            chamadas.filaCria.add(progs.PB);
                            ehCria.release();
                            semChamada.release();
                            break;
                        case 3:
                            //cria(progs.PC);
                            chamadas.filaCria.add(progs.PC);
                            ehCria.release();
                            semChamada.release();
                            break;
                        case 4:
                            //cria(progs.fibonacciTRAP);
                            chamadas.filaCria.add(progs.fibonacciTRAP);
                            ehCria.release();
                            semChamada.release();
                            break;
                        case 5:
                            //cria(progs.fatorialTRAP);
                            chamadas.filaCria.add(progs.fatorialTRAP);
                            ehCria.release();
                            semChamada.release();
                            break;
                    }
                    break;
                case 2:
                    auto = !auto;
                    menu = "MENU\n" +
                            "1 - cria processo\n" +
                            "2 - parar de executar automaticamente ao criar processos\n" +
                            "3 - começar a executar manualmente\n" +
                            "está automatico = "+auto+"\n" +
                            "0 - sair";
                    break;
                case 3:
                    teste3.release();
                    break;
                case 0:
                    break;
            }
            semanforoMenu.release();
            while(esperaTrap){}//roda apenas um menu se tiver alguma trap pedindo console
        }
    }

    // -------------------------------------------------------------------------------------------------------
    // --------------- TUDO ABAIXO DE MAIN É AUXILIAR PARA FUNCIONAMENTO DO SISTEMA - nao faz parte 

   //  -------------------------------------------- programas aa disposicao para copiar na memoria (vide carga)
   public class Programas {

	   public Word[] progMinimo = new Word[] {
		    //       OPCODE      R1  R2  P         :: VEJA AS COLUNAS VERMELHAS DA TABELA DE DEFINICAO DE OPERACOES
			//                                     :: -1 SIGNIFICA QUE O PARAMETRO NAO EXISTE PARA A OPERACAO DEFINIDA
		    new Word(Opcode.LDI, 0, -1, 999), 		
			new Word(Opcode.STD, 0, -1, 10), 
			new Word(Opcode.STD, 0, -1, 11), 
			new Word(Opcode.STD, 0, -1, 12), 
			new Word(Opcode.STD, 0, -1, 13), 
			new Word(Opcode.STD, 0, -1, 14), 
			new Word(Opcode.STOP, -1, -1, -1) };

	   public Word[] fibonacci10 = new Word[] { // mesmo que prog exemplo, so que usa r0 no lugar de r8
			new Word(Opcode.LDI, 1, -1, 0), 
			new Word(Opcode.STD, 1, -1, 20),    // 20 posicao de memoria onde inicia a serie de fibonacci gerada  
			new Word(Opcode.LDI, 2, -1, 1),
			new Word(Opcode.STD, 2, -1, 21),      
			new Word(Opcode.LDI, 0, -1, 22),       
			new Word(Opcode.LDI, 6, -1, 6),
			new Word(Opcode.LDI, 7, -1, 30),
			new Word(Opcode.LDI, 3, -1, 0), 
			new Word(Opcode.ADD, 3, 1, -1),
			new Word(Opcode.LDI, 1, -1, 0), 
			new Word(Opcode.ADD, 1, 2, -1), 
			new Word(Opcode.ADD, 2, 3, -1),
			new Word(Opcode.STX, 0, 2, -1), 
			new Word(Opcode.ADDI, 0, -1, 1), 
			new Word(Opcode.SUB, 7, 0, -1),
			new Word(Opcode.JMPIG, 6, 7, -1), 
			new Word(Opcode.STOP, -1, -1, -1),   // POS 16
			new Word(Opcode.DATA, -1, -1, -1),
			new Word(Opcode.DATA, -1, -1, -1),
			new Word(Opcode.DATA, -1, -1, -1),
			new Word(Opcode.DATA, -1, -1, -1),   // POS 20
			new Word(Opcode.DATA, -1, -1, -1),
			new Word(Opcode.DATA, -1, -1, -1),
			new Word(Opcode.DATA, -1, -1, -1),
			new Word(Opcode.DATA, -1, -1, -1),
			new Word(Opcode.DATA, -1, -1, -1),
			new Word(Opcode.DATA, -1, -1, -1),
			new Word(Opcode.DATA, -1, -1, -1),
			new Word(Opcode.DATA, -1, -1, -1),
			new Word(Opcode.DATA, -1, -1, -1)  // ate aqui - serie de fibonacci ficara armazenada
			   };   

	   public Word[] fatorial = new Word[] {
	           // este fatorial so aceita valores positivos.   nao pode ser zero
												 // linha   coment
			new Word(Opcode.LDI, 0, -1, 4),      // 0   	r0 é valor a calcular fatorial
			new Word(Opcode.LDI, 1, -1, 1),      // 1   	r1 é 1 para multiplicar (por r0)
			new Word(Opcode.LDI, 6, -1, 1),      // 2   	r6 é 1 para ser o decremento
			new Word(Opcode.LDI, 7, -1, 8),      // 3   	r7 tem posicao de stop do programa = 8
			new Word(Opcode.JMPIE, 7, 0, 0),     // 4   	se r0=0 pula para r7(=8)
			new Word(Opcode.MULT, 1, 0, -1),     // 5   	r1 = r1 * r0
			new Word(Opcode.SUB, 0, 6, -1),      // 6   	decrementa r0 1 
			new Word(Opcode.JMP, -1, -1, 4),     // 7   	vai p posicao 4
			new Word(Opcode.STD, 1, -1, 10),     // 8   	coloca valor de r1 na posição 10
			new Word(Opcode.STOP, -1, -1, -1),    // 9   	stop
			new Word(Opcode.DATA, -1, -1, -1) };  // 10   ao final o valor do fatorial estará na posição 10 da memória                                    


       public Word[] PA = new Word[] {
               // fibonnaci com valor a declarar no inicio se negativo retorna -1 na primeira posicao
               new Word(Opcode.LDI, 7, -1, 14),// numero do tamanho do fib
			   new Word(Opcode.LDI, 3, -1, 0),
			   new Word(Opcode.ADD, 3, 7, -1),
			   new Word(Opcode.LDI, 4, -1, 33),//posicao para qual ira pular (stop) *
               new Word(Opcode.LDI, 1, -1, -1),// caso negativo
               new Word(Opcode.STD, 1, -1, 38),
               new Word(Opcode.JMPIL, 4, 7, -1),//pula pra stop caso negativo *
			   new Word(Opcode.JMPIE, 4, 7, -1),//pula pra stop caso 0
               new Word(Opcode.ADDI, 7, -1, 38),// fibonacci + posição do stop
               new Word(Opcode.LDI, 1, -1, 0),
               new Word(Opcode.STD, 1, -1, 38),    // 25 posicao de memoria onde inicia a serie de fibonacci gerada
			   new Word(Opcode.SUBI, 3, -1, 1),// se 1 pula pro stop
			   new Word(Opcode.JMPIE, 4, 3, -1),
			   new Word(Opcode.ADDI, 3, -1, 1),
               new Word(Opcode.LDI, 2, -1, 1),
               new Word(Opcode.STD, 2, -1, 39),
			   new Word(Opcode.SUBI, 3, -1, 2),// se 2 pula pro stop
			   new Word(Opcode.JMPIE, 4, 3, -1),
               new Word(Opcode.LDI, 0, -1, 40),
               new Word(Opcode.LDI, 6, -1, 22),// salva posição de retorno do loop
               new Word(Opcode.LDI, 5, -1, 0),//salva tamanho
               new Word(Opcode.ADD, 5, 7, -1),
               new Word(Opcode.LDI, 7, -1, 0),//zera (inicio do loop)
               new Word(Opcode.ADD, 7, 5, -1),//recarrega tamanho
               new Word(Opcode.LDI, 3, -1, 0),
               new Word(Opcode.ADD, 3, 1, -1),
               new Word(Opcode.LDI, 1, -1, 0),
               new Word(Opcode.ADD, 1, 2, -1),
               new Word(Opcode.ADD, 2, 3, -1),
               new Word(Opcode.STX, 0, 2, -1),
               new Word(Opcode.ADDI, 0, -1, 1),
               new Word(Opcode.SUB, 7, 0, -1),
               new Word(Opcode.JMPIG, 6, 7, -1),
               new Word(Opcode.STOP, -1, -1, -1),   // POS 33 (384 inicial)
               new Word(Opcode.DATA, -1, -1, -1),
               new Word(Opcode.DATA, -1, -1, -1),
               new Word(Opcode.DATA, -1, -1, -1),
               new Word(Opcode.DATA, -1, -1, -1),
               new Word(Opcode.DATA, -1, -1, -1),   // POS 38
               new Word(Opcode.DATA, -1, -1, -1),
               new Word(Opcode.DATA, -1, -1, -1),
               new Word(Opcode.DATA, -1, -1, -1),
               new Word(Opcode.DATA, -1, -1, -1),
               new Word(Opcode.DATA, -1, -1, -1),
               new Word(Opcode.DATA, -1, -1, -1),
               new Word(Opcode.DATA, -1, -1, -1),
               new Word(Opcode.DATA, -1, -1, -1),
               new Word(Opcode.DATA, -1, -1, -1),
               new Word(Opcode.DATA, -1, -1, -1),
               new Word(Opcode.DATA, -1, -1, -1),
               new Word(Opcode.DATA, -1, -1, -1),
               new Word(Opcode.DATA, -1, -1, -1),
               new Word(Opcode.DATA, -1, -1, -1)

       };

       public Word[] PB = new Word[] {
       		//dado um inteiro em alguma posição de memória,
       		// se for negativo armazena -1 na saída; se for positivo responde o fatorial do número na saída
			   new Word(Opcode.LDI, 0, -1, 7),// numero para colocar na memoria
			   new Word(Opcode.STD, 0, -1, 16),
			   new Word(Opcode.LDD, 0, -1, 16),
			   new Word(Opcode.LDI, 1, -1, -1),
			   new Word(Opcode.LDI, 2, -1, 13),// SALVAR POS STOP
			   new Word(Opcode.JMPIL, 2, 0, -1),// caso negativo pula pro STD
			   new Word(Opcode.LDI, 1, -1, 1),
			   new Word(Opcode.LDI, 6, -1, 1),
			   new Word(Opcode.LDI, 7, -1, 13),
			   new Word(Opcode.JMPIE, 7, 0, 0), //POS 9 pula pra STD (Stop-1)
			   new Word(Opcode.MULT, 1, 0, -1),
			   new Word(Opcode.SUB, 0, 6, -1),
			   new Word(Opcode.JMP, -1, -1, 9),// pula para o JMPIE
			   new Word(Opcode.STD, 1, -1, 15),
			   new Word(Opcode.STOP, -1, -1, -1), // POS 14
			   new Word(Opcode.DATA, -1, -1, -1), //POS 15
               new Word(Opcode.DATA, -1, -1, -1)
       };

	   public Word[] PC = new Word[] {
			   //Para um N definido (10 por exemplo)
			   //o programa ordena um vetor de N números em alguma posição de memória;
			   //ordena usando bubble sort
			   //loop ate que não swap nada
			   //passando pelos N valores
			   //faz swap de vizinhos se da esquerda maior que da direita
			   new Word(Opcode.LDI, 7, -1, 5),// TAMANHO DO BUBBLE SORT (N)
			   new Word(Opcode.LDI, 6, -1, 5),//aux N
			   new Word(Opcode.LDI, 5, -1, 46),//LOCAL DA MEMORIA
			   new Word(Opcode.LDI, 4, -1, 47),//aux local memoria
			   new Word(Opcode.LDI, 0, -1, 4),//colocando valores na memoria
			   new Word(Opcode.STD, 0, -1, 46),
			   new Word(Opcode.LDI, 0, -1, 3),
			   new Word(Opcode.STD, 0, -1, 47),
			   new Word(Opcode.LDI, 0, -1, 5),
			   new Word(Opcode.STD, 0, -1, 48),
			   new Word(Opcode.LDI, 0, -1, 1),
			   new Word(Opcode.STD, 0, -1, 49),
			   new Word(Opcode.LDI, 0, -1, 2),
			   new Word(Opcode.STD, 0, -1, 50),//colocando valores na memoria até aqui - POS 13
			   new Word(Opcode.LDI, 3, -1, 25),// Posicao para pulo CHAVE 1
			   new Word(Opcode.STD, 3, -1, 54),
			   new Word(Opcode.LDI, 3, -1, 22),// Posicao para pulo CHAVE 2
			   new Word(Opcode.STD, 3, -1, 53),
			   new Word(Opcode.LDI, 3, -1, 38),// Posicao para pulo CHAVE 3
			   new Word(Opcode.STD, 3, -1, 52),
			   new Word(Opcode.LDI, 3, -1, -1),// Posicao para pulo CHAVE 4 (não usada)
			   new Word(Opcode.STD, 3, -1, 51),
			   new Word(Opcode.LDI, 6, -1, 0),//r6 = r7 - 1 POS 22
			   new Word(Opcode.ADD, 6, 7, -1),
			   new Word(Opcode.SUBI, 6, -1, 1),//ate aqui
               new Word(Opcode.JMPIEM, -1, 6, 52),//CHAVE 3 para pular quando r7 for 1 e r6 0 para interomper o loop de vez do programa
               new Word(Opcode.LDX, 0, 5, -1),//r0 e r1 pegando valores das posições da memoria POS 26
			   new Word(Opcode.LDX, 1, 4, -1),
			   new Word(Opcode.LDI, 2, -1, 0),
			   new Word(Opcode.ADD, 2, 0, -1),
			   new Word(Opcode.SUB, 2, 1, -1),
               new Word(Opcode.ADDI, 4, -1, 1),
               new Word(Opcode.SUBI, 6, -1, 1),
               new Word(Opcode.JMPILM, -1, 2, 54),//LOOP chave 1 caso neg procura prox
               new Word(Opcode.STX, 5, 1, -1),
               new Word(Opcode.SUBI, 4, -1, 1),
               new Word(Opcode.STX, 4, 0, -1),
               new Word(Opcode.ADDI, 4, -1, 1),
               new Word(Opcode.JMPIGM, -1, 6, 54),//LOOP chave 1 POS 38
               new Word(Opcode.ADDI, 5, -1, 1),
               new Word(Opcode.SUBI, 7, -1, 1),
               new Word(Opcode.LDI, 4, -1, 0),//r4 = r5 + 1 POS 41
               new Word(Opcode.ADD, 4, 5, -1),
               new Word(Opcode.ADDI, 4, -1, 1),//ate aqui
               new Word(Opcode.JMPIGM, -1, 7, 53),//LOOP chave 2
			   new Word(Opcode.STOP, -1, -1, -1), // POS 45
			   new Word(Opcode.DATA, -1, -1, -1),
               new Word(Opcode.DATA, -1, -1, -1),
               new Word(Opcode.DATA, -1, -1, -1),
               new Word(Opcode.DATA, -1, -1, -1),
               new Word(Opcode.DATA, -1, -1, -1),
               new Word(Opcode.DATA, -1, -1, -1),
               new Word(Opcode.DATA, -1, -1, -1),//52
               new Word(Opcode.DATA, -1, -1, -1),//53
               new Word(Opcode.DATA, -1, -1, -1)};//54

       public Word[] interrupcaoEndereco = new Word[] {
               new Word(Opcode.LDI, 0, -1, 999),
               new Word(Opcode.STD, 0, -1, 1025),
               new Word(Opcode.STOP, -1, -1, -1) };

       public Word[] interrupcaoInstrucao = new Word[] {
               new Word(Opcode.LDI, 10, -1, 999),
               new Word(Opcode.STOP, -1, -1, -1) };

       public Word[] interrupcaoOverflow = new Word[]{
               new Word(Opcode.LDI, 0, -1, 25),// numero para colocar na memoria
               new Word(Opcode.STD, 0, -1, 50),
               new Word(Opcode.LDD, 0, -1, 50),
               new Word(Opcode.LDI, 1, -1, -1),
               new Word(Opcode.LDI, 2, -1, 13),// SALVAR POS STOP
               new Word(Opcode.JMPIL, 2, 0, -1),// caso negativo pula pro STD
               new Word(Opcode.LDI, 1, -1, 1),
               new Word(Opcode.LDI, 6, -1, 1),
               new Word(Opcode.LDI, 7, -1, 13),
               new Word(Opcode.JMPIE, 7, 0, 0), //POS 9 pula pra STD (Stop-1)
               new Word(Opcode.MULT, 1, 0, -1),
               new Word(Opcode.SUB, 0, 6, -1),
               new Word(Opcode.JMP, -1, -1, 9),// pula para o JMPIE
               new Word(Opcode.STD, 1, -1, 15),
               new Word(Opcode.STOP, -1, -1, -1), // POS 14
               new Word(Opcode.DATA, -1, -1, -1) //POS 15
       };

       public Word[] testeTRAP_IN = new Word[]{
               new Word(Opcode.LDI, 8, -1, 1),// leitura
               new Word(Opcode.LDI, 9, -1, 4),//endereco a guardar
               new Word(Opcode.TRAP, -1, -1, -1),
               new Word(Opcode.STOP, -1, -1, -1),
               new Word(Opcode.DATA, -1, -1, -1),//. valor lido estará armazenado aqui!!!
       };

       public Word[] testeTRAP_OUT = new Word[]{
               new Word(Opcode.LDI, 0, -1, 999),
               new Word(Opcode.STD, 0, -1, 10),
               new Word(Opcode.LDI, 8, -1, 2),// escrita
               new Word(Opcode.LDI, 9, -1, 10),//endereco com valor a escrever
               new Word(Opcode.TRAP, -1, -1, -1),
               new Word(Opcode.STOP, -1, -1, -1),
               new Word(Opcode.DATA, -1, -1, -1),//. valor lido estará armazenado aqui!!!
       };

       public Word[] fibonacciTRAP = new Word[] { // mesmo que prog exemplo, so que usa r0 no lugar de r8
               new Word(Opcode.LDI, 8, -1, 1),// leitura
               new Word(Opcode.LDI, 9, -1, 40),//endereco a guardar
               new Word(Opcode.TRAP, -1, -1, -1),
               new Word(Opcode.LDD, 7, -1, 40),// numero do tamanho do fib
               new Word(Opcode.LDI, 3, -1, 0),
               new Word(Opcode.ADD, 3, 7, -1),
               new Word(Opcode.LDI, 4, -1, 36),//posicao para qual ira pular (stop) *
               new Word(Opcode.LDI, 1, -1, -1),// caso negativo
               new Word(Opcode.STD, 1, -1, 41),
               new Word(Opcode.JMPIL, 4, 7, -1),//pula pra stop caso negativo *
               new Word(Opcode.JMPIE, 4, 7, -1),//pula pra stop caso 0
               new Word(Opcode.ADDI, 7, -1, 41),// fibonacci + posição do stop
               new Word(Opcode.LDI, 1, -1, 0),
               new Word(Opcode.STD, 1, -1, 41),    // 25 posicao de memoria onde inicia a serie de fibonacci gerada
               new Word(Opcode.SUBI, 3, -1, 1),// se 1 pula pro stop
               new Word(Opcode.JMPIE, 4, 3, -1),
               new Word(Opcode.ADDI, 3, -1, 1),
               new Word(Opcode.LDI, 2, -1, 1),
               new Word(Opcode.STD, 2, -1, 42),
               new Word(Opcode.SUBI, 3, -1, 2),// se 2 pula pro stop
               new Word(Opcode.JMPIE, 4, 3, -1),
               new Word(Opcode.LDI, 0, -1, 43),
               new Word(Opcode.LDI, 6, -1, 25),// salva posição de retorno do loop
               new Word(Opcode.LDI, 5, -1, 0),//salva tamanho
               new Word(Opcode.ADD, 5, 7, -1),
               new Word(Opcode.LDI, 7, -1, 0),//zera (inicio do loop)
               new Word(Opcode.ADD, 7, 5, -1),//recarrega tamanho
               new Word(Opcode.LDI, 3, -1, 0),
               new Word(Opcode.ADD, 3, 1, -1),
               new Word(Opcode.LDI, 1, -1, 0),
               new Word(Opcode.ADD, 1, 2, -1),
               new Word(Opcode.ADD, 2, 3, -1),
               new Word(Opcode.STX, 0, 2, -1),
               new Word(Opcode.ADDI, 0, -1, 1),
               new Word(Opcode.SUB, 7, 0, -1),
               new Word(Opcode.JMPIG, 6, 7, -1),//volta para o inicio do loop
               new Word(Opcode.STOP, -1, -1, -1),   // POS 36
               new Word(Opcode.DATA, -1, -1, -1),
               new Word(Opcode.DATA, -1, -1, -1),
               new Word(Opcode.DATA, -1, -1, -1),
               new Word(Opcode.DATA, -1, -1, -1),
               new Word(Opcode.DATA, -1, -1, -1),   // POS 41
               new Word(Opcode.DATA, -1, -1, -1),
               new Word(Opcode.DATA, -1, -1, -1),
               new Word(Opcode.DATA, -1, -1, -1),
               new Word(Opcode.DATA, -1, -1, -1),
               new Word(Opcode.DATA, -1, -1, -1),
               new Word(Opcode.DATA, -1, -1, -1),
               new Word(Opcode.DATA, -1, -1, -1),
               new Word(Opcode.DATA, -1, -1, -1),
               new Word(Opcode.DATA, -1, -1, -1),
               new Word(Opcode.DATA, -1, -1, -1),
               new Word(Opcode.DATA, -1, -1, -1),
               new Word(Opcode.DATA, -1, -1, -1),
               new Word(Opcode.DATA, -1, -1, -1),
               new Word(Opcode.DATA, -1, -1, -1)
       };

       public Word[] fatorialTRAP = new Word[] {
               new Word(Opcode.LDI, 0, -1, 7),// numero para colocar na memoria
               new Word(Opcode.STD, 0, -1, 19),
               new Word(Opcode.LDD, 0, -1, 19),
               new Word(Opcode.LDI, 1, -1, -1),
               new Word(Opcode.LDI, 2, -1, 13),// SALVAR POS STOP
               new Word(Opcode.JMPIL, 2, 0, -1),// caso negativo pula pro STD
               new Word(Opcode.LDI, 1, -1, 1),
               new Word(Opcode.LDI, 6, -1, 1),
               new Word(Opcode.LDI, 7, -1, 13),
               new Word(Opcode.JMPIE, 7, 0, 0), //POS 9 pula pra STD (Stop-1)
               new Word(Opcode.MULT, 1, 0, -1),
               new Word(Opcode.SUB, 0, 6, -1),
               new Word(Opcode.JMP, -1, -1, 9),// pula para o JMPIE
               new Word(Opcode.STD, 1, -1, 18),
               new Word(Opcode.LDI, 8, -1, 2),// escrita
               new Word(Opcode.LDI, 9, -1, 18),//endereco com valor a escrever
               new Word(Opcode.TRAP, -1, -1, -1),
               new Word(Opcode.STOP, -1, -1, -1), // POS 17
               new Word(Opcode.DATA, -1, -1, -1), //POS 18
               new Word(Opcode.DATA, -1, -1, -1) //POS 19
       };

   }
}

