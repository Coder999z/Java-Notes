
* [LeetCode](#leetcode)
  * [只出现一次的数字](#%E5%8F%AA%E5%87%BA%E7%8E%B0%E4%B8%80%E6%AC%A1%E7%9A%84%E6%95%B0%E5%AD%97)
  * [求众数](#%E6%B1%82%E4%BC%97%E6%95%B0)
  * [搜索二维矩阵](#%E6%90%9C%E7%B4%A2%E4%BA%8C%E7%BB%B4%E7%9F%A9%E9%98%B5)

# LeetCode

> 记录日常刷LeetCode的题目与解法

## 只出现一次的数字

> 题目：136
>
> 给定一个非空整数数组，除了某个元素只出现一次以外，其余每个元素均出现两次。找出那个只出现了一次的元素。
>
> 说明：
>
> 你的算法应该具有线性时间复杂度。 你可以不使用额外空间来实现吗？
>
>
> 链接：https://leetcode-cn.com/problems/single-number
>
> **示例 1:**
>
> ```
> 输入: [2,2,1]
> 输出: 1
> ```
>
> **示例 2:**
>
> ```
> 输入: [4,1,2,1,2]
> 输出: 4
> ```
>
> 解答：
>
> 利用异或计算的特性，a⊕a=0，b⊕ 0 = b，并且异或运算满足交换律和结合律：a⊕b⊕a=(a⊕a)⊕b=0⊕b=b。所以对整个数组中的数字进行异或运算，最后得出的数字将会就是只出现一次的数字。
>
> ```java
> class Solution {
>     public int singleNumber(int[] nums) {
>         int temp = nums[0];
>         for(int i = 1; i < nums.length; i++) {
>             temp ^= nums[i];
>         }
>         return temp;
>     }
> }
> ```

## 求众数

> 题目：169
>
> 给定一个大小为 n 的数组，找到其中的众数。众数是指在数组中出现次数大于 ⌊ n/2 ⌋ 的元素。
>
> 你可以假设数组是非空的，并且给定的数组总是存在众数。
>
> 示例 1:
>
> ```
> 输入: [3,2,3]
> 输出: 3
> ```
>
> 示例 2:
>
> ```
> 输入: [2,2,1,1,1,2,2]
> 输出: 2
> ```
>
> 链接：https://leetcode-cn.com/problems/majority-element
>
>  解答：
>
> 使用暴力法逐个遍历和使用HashMap统计的方式比较简单，但是复杂度较高，这里介绍的是投票法。
>
> 思路：根据题目定义，众数出现的次数大于数组长度的一半，我们设定规则假设从第一个数字 a = num[0]开始count初始为1，判断a == num[1]，如果相等则count++，如果不等则count--，当count == 0时取num[i]的当前数字为temp继续循环，知道整个数组遍历完temp的数值即为众数。
>
> ```java
> class Solution {
>     public int majorityElement(int[] nums) {
>         int temp = nums[0];
>         int count = 1;
>         for(int i = 1; i <  nums.length; i++) {
>             if(temp  == nums[i]) count++;
>             else count--;
>             if(count <= 0) {
>                 temp = nums[i];
>                 count = 1;
>             }
>         }
>         return temp;
>     }
> }
> ```

## 搜索二维矩阵

> 题目：240
>
> 编写一个高效的算法来搜索 m x n 矩阵 matrix 中的一个目标值 target。该矩阵具有以下特性：
>
> 每行的元素从左到右升序排列。
> 每列的元素从上到下升序排列。
>
> 示例:
>
> 现有矩阵 matrix 如下：
>
> ```
> [
>   [1,   4,  7, 11, 15],
>   [2,   5,  8, 12, 19],
>   [3,   6,  9, 16, 22],
>   [10, 13, 14, 17, 24],
>   [18, 21, 23, 26, 30]
> ]
> ```
>
> 给定 target = 5，返回 true。
>
> 给定 target = 20，返回 false。
>
> 来源：力扣（LeetCode）
> 链接：https://leetcode-cn.com/problems/search-a-2d-matrix-ii
>
> 解答：
>
> 暴力遍历法就不介绍了，如果不是想不出别的方式不建议使用它。
>
> 由于此二维矩阵本身是有按行列排序，我们选取特殊位置的点，例如左下角的18，如果target比18小则向下移动一行，如果比18大则向左移动一列。因为是从上到下和从左到右排序的，不难发现比18大的在右侧，比18小的在上面。
>
> ```java
> class Solution {
>     public boolean searchMatrix(int[][] matrix, int target) {
>         //行
>         int cow = matrix.length - 1;
>         //列
>         int col = 0;
>         
>         while(cow >= 0 && col <= (matrix[0].length - 1)) {
>             if (matrix[cow][col] == target) return true;
>             else if (matrix[cow][col] > target) cow--;
>             else col++;
>         }
>         return false;
>     }
> }
> ```
>
> 

