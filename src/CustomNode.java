public class CustomNode {
    Boolean parent;
    int value;
    CustomNode leftChild;
    CustomNode rightChild;

    public CustomNode(int value, boolean isParent) {
        this.value = value;
        this.leftChild = null;
        this.rightChild = null;
        this.parent = isParent;
    }
}
