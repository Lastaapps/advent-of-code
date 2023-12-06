package main

import (
	"fmt"
	"math"
)

var input_test = [][2]int{
    {7,    9},
    {15,  40},
    {30, 200},
}
               
var input_prod = [][2]int{ 
    {44,  283},
    {70, 1134},
    {70, 1134},
    {80, 1491},
}

func comp_dist(charging int, time int) int {
    return charging*(time - charging)
}

func count_variants(time int, dist int) int {
    var d int = time * time - 4 * dist
    var d_sqrt float64 = math.Sqrt(float64(d))
    lowerf := (float64(time) - d_sqrt) / 2
    upperf := (float64(time) + d_sqrt) / 2
    lower := int(math.Ceil(lowerf))
    upper := int(math.Floor(upperf))

    res := upper - lower + 1

    // Fix values on the edge
    if comp_dist(lower, time) <= dist { res -= 1 }
    if comp_dist(upper, time) <= dist { res -= 1 }

    return res
}

func part_01(data [][2]int) int {
    res := 1
    for _, value := range data {
        val := count_variants(value[0], value[1])
        fmt.Println(val)
        res *= val
    }
    return res
}
func part_02(data [][2]int) int {
    return 0
}

func main() {
    // data := input_test
    data := input_prod
    fmt.Println("Part 01: ", part_01(data)) // 219849
    fmt.Println("Part 02: ", part_02(data))
}
