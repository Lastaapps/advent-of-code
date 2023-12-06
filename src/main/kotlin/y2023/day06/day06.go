package main

import (
	"fmt"
	"math"
)

var inpun01_test = [][2]uint64{
    {7,    9},
    {15,  40},
    {30, 200},
}
var input02_test = [2]uint64{71530, 940200}
               
var input01_prod = [][2]uint64{ 
    {44,  283},
    {70, 1134},
    {70, 1134},
    {80, 1491},
}
var input02_prod = [2]uint64{44707080, 283113411341491}

func comp_dist(charging uint64, time uint64) uint64 {
    return charging*(time - charging)
}

func count_variants(time uint64, dist uint64) uint64 {
    var d uint64 = time * time - 4 * dist
    var d_sqrt float64 = math.Sqrt(float64(d))
    lowerf := (float64(time) - d_sqrt) / 2
    upperf := (float64(time) + d_sqrt) / 2
    lower := uint64(math.Ceil(lowerf))
    upper := uint64(math.Floor(upperf))

    res := upper - lower + 1

    // Fix values on the edge
    if comp_dist(lower, time) <= dist { res -= 1 }
    if comp_dist(upper, time) <= dist { res -= 1 }

    return res
}

func part_01(data [][2]uint64) uint64 {
    var res uint64 = 1
    for _, value := range data {
        val := count_variants(value[0], value[1])
        res *= val
    }
    return res
}
func part_02(data [2]uint64) uint64 {
    return count_variants(data[0], data[1])
}

func main() {
    // data01 := input01_test
    data01 := input01_prod
    fmt.Println("Part 01: ", part_01(data01)) // 219849

    // data02 := input02_test
    data02 := input02_prod
    fmt.Println("Part 02: ", part_02(data02))
}
