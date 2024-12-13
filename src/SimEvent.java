public class SimEvent implements Comparable<SimEvent>{

    public static enum eventType {INSPECT_COMPONENT_1,INSPECT_COMPONENT_2,INSPECT_COMPONENT_3,PRODUCE_P1,PRODUCE_P2,PRODUCE_P3, END_OF_SIM};
    private eventType eType;        // Type of the event
    private Double eTime;          // Event Time
    private ComponentBuffer component;
    private ComponentBuffer component2;

    public SimEvent(eventType eType, double eTime) {
        this.eType = eType;
        this.eTime = eTime;
    }

    @Override
    public int compareTo(SimEvent ev) {
        return this.geteTime().compareTo(ev.geteTime());
    }

    public eventType geteType() {
        return eType;
    }

    public void seteType(eventType eType) {
        this.eType = eType;
    }

    public Double geteTime() {
        return eTime;
    }

    public void seteTime(double eTime) {
        this.eTime = eTime;
    }

    public ComponentBuffer getComponent() {
        return component;
    }

    public void setComponent(ComponentBuffer componentBuffer) {
        this.component = componentBuffer;
    }

    public void seteTime(Double eTime) {
        this.eTime = eTime;
    }

    public ComponentBuffer getComponent2() {
        return component2;
    }

    public void setComponent2(ComponentBuffer component2) {
        this.component2 = component2;
    }
}