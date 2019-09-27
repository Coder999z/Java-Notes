package datastructure;


import java.util.LinkedList;
import java.util.Queue;

/**
 * @Author ：
 * @Date ： 2019.09.26
 * @Description ：Build Huffman Tree
 * @Version :
 */
public class HuffmanNode {

    private int weight;
    private HuffmanNode left;
    private HuffmanNode right;

    public HuffmanNode() {
    }

    public HuffmanNode(int weight) {
        this.weight = weight;
    }

    public HuffmanNode(int weight, HuffmanNode left, HuffmanNode right) {
        this.weight = weight;
        this.left = left;
        this.right = right;
    }

    public static void main(String[] args) {
        HuffmanNode[] array = new HuffmanNode[]{
                new HuffmanNode(5),
                new HuffmanNode(6),
                new HuffmanNode(7),
                new HuffmanNode(8),
                new HuffmanNode(16)};
        HuffmanNode run = new HuffmanNode();
        HuffmanNode root = run.buildHuffmanTree(array);
        run.print(root);

        // 1. 输入数组
        // 2. 构造大跟堆，排序
        // 3. 取出末尾两个最小的，生成新的节点
        // 4. 将新节点插入末尾，并重新维护顺序
        // 5. 循环操作 length - 1 次

        //完全二叉树获得最后非叶节点公式 数组长度/2-1
        //左子节点：2n + 1，右子节点2n + 2
    }

    private HuffmanNode buildHuffmanTree(HuffmanNode[] array) {
        int length = array.length;
        //初始化大根堆
        heapSort(array, length);
        //循环n-1次
        for (int i = length - 1; i > 0; i--) {
            HuffmanNode a = array[i];
            HuffmanNode b = array[i - 1];
            HuffmanNode parent = new HuffmanNode((a.weight + b.weight), a, b);
            array[i - 1] = parent;
            array[i] = null;
            heapSort(array, i);
        }
        return array[0];
    }

    private void heapSort(HuffmanNode[] array, int length) {
        //构造小根堆
        for (int i = ((length >> 1) - 1); i >= 0; i--) {
            buildHeap(array, i, length);
        }
        //排序形成大跟堆
        for (int i = length - 1; i >= 0; i--) {
            swap(array, 0, i);
            buildHeap(array, 0, i);
        }
    }

    /**
     * @return ：void
     * @Param ：[array, index构造根堆的节点索引, length数组长度]
     * @Description：
     * @Exception :
     * @Author：2019/9/27
     */
    private void buildHeap(HuffmanNode[] array, int index, int length) {
        //左子节点索引
        int a = 0;
        //循环到叶子节点为止
        while (((index << 1) + 1) < length) {
            a = (index << 1) + 1;
            //判断右子节点是否存在
            if ((a + 1) < length) {
                //选出左右子节点中较小的
                if (compare(array, a + 1, a) < 0) {
                    //取右子节点索引
                    a++;
                }
            }
            if (compare(array, a, index) < 0) {
                swap(array, a, index);
                index = a;
            } else {
                break;
            }
        }
    }

    private int compare(HuffmanNode[] array, int a, int b) {
        return array[a].weight - array[b].weight;
    }

    private void swap(HuffmanNode[] array, int a, int b) {
        HuffmanNode temp = array[a];
        array[a] = array[b];
        array[b] = temp;
        temp = null;
    }

    //按层遍历
    private void print(HuffmanNode root) {
        Queue<HuffmanNode> queue = new LinkedList<>();
        HuffmanNode tail = root;
        HuffmanNode temp = null;
        queue.add(root);
        while (!queue.isEmpty()) {
            HuffmanNode pop = queue.poll();
            if ((temp = pop.left) != null) queue.add(temp);
            if ((temp = pop.right) != null) queue.add(temp);
            System.out.print(pop.weight + " ");
            if (pop == tail) {
                tail = temp;
                System.out.println();
            }
        }
    }

}
