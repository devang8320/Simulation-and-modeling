import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class SimModel {

    private static double insp1_idle_time = 0, insp2_idle_time = 0,total_p1_ctr = 0,total_p2_ctr = 0,total_p3_ctr = 0;
    private static double clock, duration;
    private static boolean initPhase = false;
    private static double component1Average, component2Average,component3Average, ws1Average,ws2Average, ws3Average;
    private static boolean inspector1Blocked =false, inspector2Component2Blocked=false,inspector2Component3Blocked=false, isW1Busy=false, isW2Busy=false,isW3Busy=false;
    private static double inspector1BlockStart = 0,inspector2BlockStart = 0;
    private static Random inspector2Rnd, rng,workstationRNG;
    private static Queue<SimEvent> eventQueue;
    private static Queue<ComponentBuffer> W1_C1, W2_C1,W3_C1, W2_C2,W3_C3;
    private static double c1_w1 = 0, c1_w2 = 0, c1_w3 =0 , c2_w2 =0,c3_w3=0;
    private static double num_of_c1_w1 = 0,num_of_c1_w2 = 0,num_of_c1_w3 = 0, num_of_c2 = 0, num_of_c3 = 0;
    private static HashMap<Integer,Double>  time_avg_c1_w1,time_avg_num_of_c1_w2,time_avg_num_of_c1_w3, time_avg_num_of_c2, time_avg_num_of_c3;
    private static double timeAvgStartTime = 0;

    public static void main(String[]  args){
        initialize();

        System.out.print("\n-----------------------------------------------------------\n");
        System.out.print("Start sim\n");

        while ((clock <= duration) && !(eventQueue.isEmpty())) {
            SimEvent currEvent = eventQueue.poll();

            if (currEvent != null) {
                clock = currEvent.geteTime();
                System.out.print("\nClock = " + round(clock) +", ");
                execute_event(currEvent);
            }
            System.out.print((inspector1Blocked?"INS1 blocked= " + true :"") +
                    (inspector2Component2Blocked?" INS2_C2 blocked= " + true :"")
                    + (inspector2Component3Blocked?" INS2_C3 blocked= " + true :"") +
                    "  W1_C1 = "  + W1_C1.size() +"  W2_C1 = "
                    + W2_C1.size() + "  W2_C2 = "  + W2_C2.size() + "  W3_C1 = "  + W3_C1.size() +"  W3_C3 = "  + W3_C3.size());
        }
        if(inspector2BlockStart != 0){
            insp2_idle_time += clock - inspector2BlockStart;
        }
        while(!W1_C1.isEmpty()){
            c1_w1 +=  clock- W1_C1.remove().getWorkstationBufferArrivalTime();
        }
        while(!W2_C1.isEmpty()){

            c1_w2 +=  clock- W2_C1.remove().getWorkstationBufferArrivalTime();
        }
        while(!W2_C2.isEmpty()){
            c2_w2 +=  clock- W2_C2.remove().getWorkstationBufferArrivalTime();
        }
        while(!W3_C1.isEmpty()){
            c1_w3 +=  clock- W3_C1.remove().getWorkstationBufferArrivalTime();

        }
        while(!W3_C3.isEmpty()){
            c3_w3 +=  clock- W3_C3.remove().getWorkstationBufferArrivalTime();
        }

        generateReport();
    }

    private static void generateReport(){
        double THRU1 = total_p1_ctr;
        double THRU2 = total_p2_ctr;
        double THRU3 = total_p3_ctr;

        System.out.print("\n-----------------------------------------------------------\n");
        System.out.print("*****Statistics*****\n");
        System.out.print("Clock = " + clock + "min\n");
        System.out.print("Product1 = " + (total_p1_ctr) + "\n");
        System.out.print("Product2 = " + (total_p2_ctr) + "\n");
        System.out.print("Product3 = " + (total_p3_ctr) + "\n");
        System.out.print("Products = " + (total_p1_ctr+total_p2_ctr+total_p3_ctr) + "\n");
        System.out.print("Inspector1 Idle Time = " + insp1_idle_time/3000 + "\n");
        System.out.print("Inspector2 Idle Time = " + round(insp2_idle_time/3000) + "\n");
        System.out.print("W1 Throughput = " + round(THRU1) + "products\n");
        System.out.print("W2 Throughput = " + round(THRU2) + "products\n");
        System.out.print("W3 Throughput = " + round(THRU3) + "products\n");

        System.out.println("***** Little's Law Verification ******");
        System.out.println("C1->W1 total time: " + round(c1_w1) + "min");
        System.out.println("C1->W2 total time: " + round(c1_w2) + "min");
        System.out.println("C1->W3 total time: " + round(c1_w3) + "min");
        System.out.println("C2->W2 total time: " + round(c2_w2) + "min");
        System.out.println("C3->W3 total time: " + round(c3_w3) + "min");

        System.out.println("Total # of C1->W1 Components: " + round(num_of_c1_w1));
        System.out.println("Total # of C1->W2 Components: " + round(num_of_c1_w2));
        System.out.println("Total # of C1->W3 Components: " + round(num_of_c1_w3));
        System.out.println("Total # of C2->W2 Components: " + round(num_of_c2));
        System.out.println("Total # of C3->W3 Components: " + round(num_of_c3));

        System.out.println();

        System.out.println("Calculate Average Number of Components in the system with Little's Law");
        System.out.println("C1->W1) w = " + round((c1_w1)/num_of_c1_w1) + " lamda= " + round(num_of_c1_w1/getSystemDuration())
            + " w*lambda = " + round((c1_w1/num_of_c1_w1)*(num_of_c1_w1/getSystemDuration())));
        System.out.println("C1->W2) w = " + round((c1_w2)/num_of_c1_w2) + " lamda= " + round(num_of_c1_w2/getSystemDuration())
                + " w*lambda = " + round((c1_w2/num_of_c1_w2)*(num_of_c1_w2/getSystemDuration())));
        System.out.println("C1->W3) w = " + round((c1_w3)/num_of_c1_w3)+ " lamda= " + round(num_of_c1_w3/getSystemDuration())
                + " w*lambda = " + round((c1_w3/num_of_c1_w3)*(num_of_c1_w3/getSystemDuration())));
        System.out.println("C2->W2) w = " + round((c2_w2)/num_of_c2)+ " lamda= " + round(num_of_c2/getSystemDuration())
                + " w*lambda = " + round((c2_w2/num_of_c2)*(num_of_c2/getSystemDuration())));
        System.out.println("C3->W3) w = " + round((c3_w3)/num_of_c3)+ " lamda= " + round(num_of_c3/getSystemDuration())
                + " w*lambda = " + round((c3_w3/num_of_c3)*(num_of_c3/getSystemDuration())));
        System.out.println();
        System.out.println("Calculate Average Number of Components in the system manually");
        System.out.println("C1->W1) L = " + round(calcL(time_avg_c1_w1)));
        System.out.println("C1->W2) L = " + round(calcL(time_avg_num_of_c1_w2)));
        System.out.println("C1->W3) L = " + round(calcL(time_avg_num_of_c1_w3)));
        System.out.println("C2->W2) L = " + round(calcL(time_avg_num_of_c2)));
        System.out.println("C3->W3) L = " + round(calcL(time_avg_num_of_c3)));

        System.out.println();

        System.out.print("W1 Throughput = " + round(THRU1) + "products\n");
        System.out.print("W2 Throughput = " + round(THRU2) + "products\n");
        System.out.print("W3 Throughput = " + round(THRU3) + "products\n");
        System.out.print("Inspector1 Idle Time = " + insp1_idle_time/3000 + "\n");
        System.out.print("Inspector2 Idle Time = " + round(insp2_idle_time/3000) + "\n");
        System.out.print("Products = " + (total_p1_ctr+total_p2_ctr+total_p3_ctr) + "\n");
       }

    private static void initialize(){
        component1Average = readAverage("servinsp1.dat");
        component2Average = readAverage("servinsp22.dat");
        component3Average = readAverage("servinsp23.dat");
        ws1Average = readAverage("ws1.dat");
        ws2Average = readAverage("ws2.dat");
        ws3Average = readAverage("ws3.dat");

        inspector2Rnd = new  Random();
        rng = new  Random();
        workstationRNG = new Random();

        clock = 0;
        duration = 50*60; // 50hrs =>3000min

        eventQueue = new PriorityQueue<>();            // Initializing the FEL and waiting queues

        // Workstation 1
        W1_C1 = new LinkedList<>();

        // Workstation 2
        W2_C1 = new LinkedList<>();
        W2_C2 = new LinkedList<>();

        // Workstation 3
        W3_C1 = new LinkedList<>();
        W3_C3 = new LinkedList<>();

        time_avg_c1_w1 = new HashMap<>();
        time_avg_num_of_c1_w2= new HashMap<>();
        time_avg_num_of_c1_w3= new HashMap<>();
        time_avg_num_of_c2 = new HashMap<>();
        time_avg_num_of_c3= new HashMap<>();

        schedule_event(SimEvent.eventType.INSPECT_COMPONENT_1, null, null);
        schedule_event(getRandomComponent(),null,null);
    }

    private static void execute_event(SimEvent event){

        addTimeAvg(time_avg_c1_w1, timeAvgStartTime, getC1W1Components());
        addTimeAvg( time_avg_num_of_c1_w2,timeAvgStartTime,getC1W2Components());
        addTimeAvg(time_avg_num_of_c1_w3,timeAvgStartTime,getC1W3Components());
        addTimeAvg(time_avg_num_of_c2 ,timeAvgStartTime,getC2W2Components());
        addTimeAvg(time_avg_num_of_c3,timeAvgStartTime,getC3W3Components());
        timeAvgStartTime = clock;

        switch(event.geteType()){
            case INSPECT_COMPONENT_1:
                System.out.print("Inspector 1 Inspected ComponentBuffer 1, ");
                inspector1Blocked  = findC1Buffer();
                if(inspector1Blocked){
                    System.out.print("Inspector 1 Blocked, ");
                    inspector1BlockStart = clock;
                }else {
                    schedule_event(SimEvent.eventType.INSPECT_COMPONENT_1,null,null);
                }

                break;
            case INSPECT_COMPONENT_2:
                System.out.print("Inspector 2 Inspected ComponentBuffer 2, ");
                if(W2_C2.size() == 2){
                    System.out.print("Inspector 2 Blocked, ");
                    inspector2Component2Blocked = true;
                    inspector2BlockStart = clock;
                }else {
                    W2_C2.offer(new ComponentBuffer(clock));
                    if(initPhaseDone()) {
                        num_of_c2++;
                    }
                    if(W2_C1.size() > 0 && !isW2Busy){
                        isW2Busy = true;
                        ComponentBuffer c1 =  W2_C1.remove();
                        ComponentBuffer c2 =  W2_C2.remove();
                        if(initPhaseDone()) {
                            c1.incTotalWaitTime(clock - c1.getWorkstationBufferArrivalTime());
                            c2.incTotalWaitTime(clock - c2.getWorkstationBufferArrivalTime());
                        }
                        schedule_event(SimEvent.eventType.PRODUCE_P2,c1,c2);
                    }
                    schedule_event(getRandomComponent(),null,null);
                }
                break;
            case INSPECT_COMPONENT_3:
                System.out.print("Inspector 2 Inspected ComponentBuffer 3, ");
                if(W3_C3.size() == 2){
                    System.out.print("Inspector 2 Blocked, ");
                    inspector2Component3Blocked = true;
                    inspector2BlockStart = clock;
                }else {
                    W3_C3.offer(new ComponentBuffer(clock));
                    if(initPhaseDone()) {
                        num_of_c3++;
                    }
                    if(W3_C1.size() > 0 && !isW3Busy){
                        isW3Busy = true;
                        ComponentBuffer c3 =  W3_C3.remove();
                        ComponentBuffer c1 =  W3_C1.remove();
                        if(initPhaseDone()) {

                            c1.incTotalWaitTime(clock - c1.getWorkstationBufferArrivalTime());
                            c3.incTotalWaitTime(clock - c3.getWorkstationBufferArrivalTime());
                        }
                        schedule_event(SimEvent.eventType.PRODUCE_P3,c1,c3);
                    }
                    schedule_event(getRandomComponent(),null,null);
                }
                break;
            case PRODUCE_P1:
                System.out.print("Workstation 1 Produced Product 1, ");
                if(initPhaseDone()) {
                    total_p1_ctr++;
                    c1_w1 += event.getComponent().getTotalWaitTime();
                }
                if(W1_C1.size() > 0){
                   startProducingW1();
                }else{
                    isW1Busy = false;
                }
                break;
            case PRODUCE_P2:
                System.out.print("Workstation 2 Produced Product 2, ");
                if(initPhaseDone()) {
                    total_p2_ctr++;
                    c1_w2 += event.getComponent().getTotalWaitTime();
                    c2_w2 += event.getComponent2().getTotalWaitTime();
                }

                if(W2_C1.size() > 0 && W2_C2.size() > 0){
                   startProducingW2();
                }else{
                    isW2Busy = false;
                }
                break;
            case PRODUCE_P3:
                System.out.print("Workstation 3 Produced Product 3, ");
                if(initPhaseDone()) {
                    total_p3_ctr++;
                    c1_w3 += event.getComponent().getTotalWaitTime();
                    c3_w3 += event.getComponent2().getTotalWaitTime();
                }

                if(W3_C1.size() > 0&& W3_C3.size() > 0){
                    startProducingW3();
                }else{
                    isW3Busy = false;
                }
                break;

            case END_OF_SIM:
                eventQueue.clear();
                break;
        }
    }

    private static void startProducingW1(){
        ComponentBuffer c1 =  W1_C1.remove();
        if(initPhaseDone()) {
            c1.incTotalWaitTime(clock - c1.getWorkstationBufferArrivalTime());
        }
        if(inspector1Blocked){
            inspector1Blocked  = findC1Buffer();
            insp1_idle_time += clock - inspector1BlockStart;
            inspector1BlockStart = 0;
            schedule_event(SimEvent.eventType.INSPECT_COMPONENT_1,null,null);
        }

        isW1Busy = true;
        schedule_event(SimEvent.eventType.PRODUCE_P1,c1,null);
    }

    private static void startProducingW2(){
        ComponentBuffer c1 =  W2_C1.remove();
        ComponentBuffer c2 =  W2_C2.remove();
        if(initPhaseDone()) {
            c1.incTotalWaitTime(clock - c1.getWorkstationBufferArrivalTime());
            c2.incTotalWaitTime(clock - c2.getWorkstationBufferArrivalTime());
        }
        if(inspector2Component2Blocked){
            W2_C2.offer(new ComponentBuffer(clock));
            if(initPhaseDone()) {
                num_of_c2 ++;
                insp2_idle_time += clock - inspector2BlockStart;
                inspector2BlockStart = 0;
            }
            inspector2Component2Blocked = false;
            schedule_event(getRandomComponent(),null,null);
        }
        if(inspector1Blocked){
            inspector1Blocked  = findC1Buffer();
            insp1_idle_time += clock - inspector1BlockStart;
            inspector1BlockStart = 0;
            schedule_event(SimEvent.eventType.INSPECT_COMPONENT_1,null,null);
        }
        isW2Busy = true;
        schedule_event(SimEvent.eventType.PRODUCE_P2,c1,c2);
    }

    private static void startProducingW3(){
        ComponentBuffer c1 =  W3_C1.remove();
        ComponentBuffer c3 =  W3_C3.remove();
        if(initPhaseDone()) {
            c1.incTotalWaitTime(clock - c1.getWorkstationBufferArrivalTime());
            c3.incTotalWaitTime(clock - c3.getWorkstationBufferArrivalTime());
        }
        if(inspector2Component3Blocked){
            W3_C3.offer(new ComponentBuffer(clock));
            if(initPhaseDone()) {
                num_of_c3 ++;
                insp2_idle_time += clock - inspector2BlockStart;
                inspector2BlockStart = 0;
            }
            inspector2Component3Blocked = false;
            schedule_event(getRandomComponent(),null,null);
        }
        if(inspector1Blocked){
            inspector1Blocked  = findC1Buffer();
            insp1_idle_time += clock - inspector1BlockStart;
            inspector1BlockStart = 0;
            schedule_event(SimEvent.eventType.INSPECT_COMPONENT_1,null,null);
        }
        isW3Busy = true;
        schedule_event(SimEvent.eventType.PRODUCE_P3,c1,c3);
    }

    private static void schedule_event(SimEvent.eventType eType, ComponentBuffer c1, ComponentBuffer c2){
        double newRN = -1;
        SimEvent event = null;
        switch(eType){
            case INSPECT_COMPONENT_1:
                newRN = generate_service_time(component1Average);

                event = new SimEvent(eType, clock + newRN);
                break;
            case INSPECT_COMPONENT_2:
                newRN = generate_service_time(component2Average);
                event = new SimEvent(eType, clock + newRN);

                break;
            case INSPECT_COMPONENT_3:
                newRN = generate_service_time(component3Average);
                event = new SimEvent(eType, clock + newRN);

                break;
            case PRODUCE_P1:
                newRN = generate_service_time(ws1Average);
                event = new SimEvent(eType, clock + newRN);
                event.setComponent(c1);
                if(initPhaseDone()) {
                    if (clock + newRN >= duration) {
                        c1.incTotalWaitTime(3000 - clock);
                        c1_w1 += c1.getTotalWaitTime();
                    } else {
                        c1.incTotalWaitTime(newRN);
                    }
                }
                break;
            case PRODUCE_P2:
                newRN = generate_service_time(ws2Average);
                event = new SimEvent(eType, clock + newRN);
                event.setComponent(c1);
                event.setComponent2(c2);
                if(initPhaseDone()) {
                    if (clock + newRN >= duration) {
                        c1.incTotalWaitTime(3000 - clock);
                        c1_w2 += c1.getTotalWaitTime();
                        c2.incTotalWaitTime(3000 - clock);
                        c2_w2 += c2.getTotalWaitTime();
                    } else {
                        c1.incTotalWaitTime(newRN);
                        c2.incTotalWaitTime(newRN);
                    }
                }
                break;
            case PRODUCE_P3:
                newRN = generate_service_time(ws3Average);
                if(clock+newRN >= duration){
                    break;
                }
                event = new SimEvent(eType, clock + newRN);
                if(initPhaseDone()) {
                    if (clock + newRN >= duration) {
                        c1.incTotalWaitTime(3000 - clock);
                        c1_w3 += c1.getTotalWaitTime();
                        c2.incTotalWaitTime(3000 - clock);
                        c3_w3 += c2.getTotalWaitTime();
                    } else {
                        c1.incTotalWaitTime(newRN);
                        c2.incTotalWaitTime(newRN);
                    }
                }
                event.setComponent(c1);
                event.setComponent2(c2);
                break;
        }
        if(clock+newRN >= duration){
            event = new SimEvent(SimEvent.eventType.END_OF_SIM, 3000);
            SimEvent newEVT = event;
            eventQueue.offer(newEVT);
        }else {
            SimEvent newEVT = event;//new SimEvent(eType, clock + newRN);
            eventQueue.offer(newEVT);
        }
    }

/**

 ALTERNATE #1 - Demand based policy


    private static boolean findC1Buffer(){
        if(W1_C1.size() == 2 && W2_C1.size() == 2 && W3_C1.size() == 2){
            return true;
        }

        int w2c1Needed = W2_C2.size() - W2_C1.size();
        int w3c1Needed = W3_C3.size() - W3_C1.size();

        if(w2c1Needed <=0 && w3c1Needed<=0 && W1_C1.size() < 2){
            // Send to W1
            System.out.print("Send to W1, ");
            W1_C1.offer(new ComponentBuffer(clock));
            if (initPhaseDone()) {
                num_of_c1_w1++;
            }
            if (!isW1Busy) {
                startProducingW1();
            }
            return false;
        }else if(w2c1Needed >= w3c1Needed && W2_C1.size() < 2){
            System.out.print("Send to W2, ");
            W2_C1.offer(new ComponentBuffer(clock));
            if(initPhaseDone()) {
                num_of_c1_w2++;
            }
            if(W2_C2.size() > 0 && !isW2Busy){
                startProducingW2();
            }
            return false;
        }else if(w3c1Needed >= w2c1Needed && W3_C1.size() < 2){
            System.out.print("Send to W3, ");
            W3_C1.offer(new ComponentBuffer(clock));
            if(initPhaseDone()) {
                num_of_c1_w3++;
            }
            if(W3_C3.size() > 0 && !isW3Busy){
                startProducingW3();
            }
            return false;
        }

        return false;
    }
**/

/**
    Alternate #2 - Random workstation policy

    private static boolean findC1Buffer(){
        ArrayList<Queue<ComponentBuffer>> buffers = new ArrayList<>();

        if(W1_C1.size() < 2) {
            buffers.add(W1_C1);
        }
        if(W2_C1.size() < 2) {
            buffers.add(W2_C1);
        }
        if(W3_C1.size() < 2) {
            buffers.add(W3_C1);
        }

        if(buffers.isEmpty()){
            return true;
        }

        int index = workstationRNG.nextInt(buffers.size());
        Queue<ComponentBuffer> buffer = buffers.get(index);
        if(buffer == (W1_C1)) {
            // Send to W1
            System.out.print("Send to W1, ");
            W1_C1.offer(new ComponentBuffer(clock));
            if (initPhaseDone()) {
                num_of_c1_w1++;
            }
            if (!isW1Busy) {
                startProducingW1();
            }
            return false;
        }else if(buffer == (W2_C1)){
                System.out.print("Send to W2, ");
                W2_C1.offer(new ComponentBuffer(clock));
                if(initPhaseDone()) {
                    num_of_c1_w2++;
                }
                if(W2_C2.size() > 0 && !isW2Busy){
                    startProducingW2();
                }
                return false;
        }else if(buffer == (W3_C1)){
                System.out.print("Send to W3, ");
                W3_C1.offer(new ComponentBuffer(clock));
                if(initPhaseDone()) {
                    num_of_c1_w3++;
                }
                if(W3_C3.size() > 0 && !isW3Busy){
                    startProducingW3();
                }
                return false;
        }
        return false;
    }
*/

    private static boolean findC1Buffer(){

        if(W1_C1.size() == 2 && W2_C1.size() == 2 && W3_C1.size() == 2){
            return true;
        }

        if(W1_C1.size() <2 && (W1_C1.size() <= W2_C1.size() &&  W1_C1.size()<= W3_C1.size())){
            System.out.print("Send to W1, ");
            W1_C1.offer(new ComponentBuffer(clock));
            if(initPhaseDone()) {
                num_of_c1_w1++;
            }
            if(!isW1Busy){
               startProducingW1();
            }
            return false;
        }else if(W2_C1.size() <2 && (W2_C1.size() < W1_C1.size() &&  W2_C1.size()<= W3_C1.size())){
            System.out.print("Send to W2, ");
            W2_C1.offer(new ComponentBuffer(clock));
            if(initPhaseDone()) {
                num_of_c1_w2++;
            }
            if(W2_C2.size() > 0 && !isW2Busy){
                startProducingW2();
            }
            return false;
        }else if(W3_C1.size() <2){
            System.out.print("Send to W3, ");
            W3_C1.offer(new ComponentBuffer(clock));
            if(initPhaseDone()) {
                num_of_c1_w3++;
            }
            if(W3_C3.size() > 0 && !isW3Busy){
                startProducingW3();
            }
            return false;
        }else{
            return true;
        }
    }

    private static SimEvent.eventType getRandomComponent(){
        //Random r = new Random(1000);
        int chance = inspector2Rnd.nextInt(2);
        if (chance == 1) {
           return SimEvent.eventType.INSPECT_COMPONENT_2;
        } else {
            return SimEvent.eventType.INSPECT_COMPONENT_3;
        }
    }

    private static double generate_service_time( double average){
        //Random rng = new Random(1000);
        return ((-1*average) * (Math.log(1-rng.nextDouble())));
    }

    private static double readAverage(String file){
        return readData(new File(file))
                .stream()
                .mapToDouble(a -> a)
                .average().orElse(0.0);
    }

    private static List<Double> readData(File file){
        Scanner scnr = null;
        List<Double> data = new ArrayList<>();

        try {
            scnr = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while(scnr.hasNextDouble()){
            Double time = scnr.nextDouble();
            data.add(time);
        }
        return data;
    }

    private static double round(double num){
        return Math.round(num * 10000.0) / 10000.0;
    }

    private static boolean initPhaseDone(){
        if(!initPhase){
            return true;
        }
        return clock/60 >=1;
    }

    private static double getSystemDuration(){
        if(!initPhase){
            return duration;
        }
        return duration -1;
    }

    private static int getC1W1Components(){
        return W1_C1.size() + (isW1Busy?1:0);
    }

    private static int getC1W2Components(){
        return W2_C1.size() + (isW2Busy?1:0);
    }
    private static int getC1W3Components(){
        return W3_C1.size() + (isW3Busy?1:0);
    }
    private static int getC2W2Components(){
        return W2_C2.size() + (isW2Busy?1:0);
    }
    private static int getC3W3Components(){
        return W3_C3.size() + (isW3Busy?1:0);
    }

    private static HashMap<Integer,Double> addTimeAvg(HashMap<Integer,Double> map, double startTime,int currNum){
        if(map.containsKey(currNum)){
            map.put(currNum,  map.get(currNum)  + (clock-startTime));
        }else{
            map.put(currNum, (clock-startTime));
        }
        return map;
    }

    private static double calcL(HashMap<Integer,Double> map){
        double l = 0;
        for (Map.Entry<Integer, Double> entry: map.entrySet()) {
            l+= entry.getKey() * entry.getValue();
        }
        return l/getSystemDuration();
    }
}
