use regex::Regex;
use std::fs;

type Point = (u32, u32, u32);

#[derive(Debug)]
struct Cube {
    from: Point,
    to: Point,
}

fn read_input(file_path: &str) -> Vec<Cube> {
    let data = fs::read_to_string(file_path).unwrap();
    let re = Regex::new(r"([0-9]+),([0-9]+),([0-9]+)~([0-9]+),([0-9]+),([0-9]+)").unwrap();
    re.captures_iter(&data)
        .map(|c| {
            let (_, coords) = c.extract();
            let [mut x1, mut y1, mut z1, mut x2, mut y2, mut z2] =
                coords.map(|it| it.parse().unwrap());

            if x1 > x2 {
                (x1, x2) = (x2, x1);
            }
            if y1 > y2 {
                (y1, y2) = (y2, y1);
            }
            if z1 > z2 {
                (z1, z2) = (z2, z1);
            }
            Cube {
                from: (x1, y1, z1),
                to: (x2, y2, z2),
            }
        })
        .collect()
}

fn part01(file_path: &str) -> usize {
    let mut cubes = read_input(file_path);
    cubes.sort_by(|a, b| a.from.2.cmp(&b.from.2));

    let mut needed = vec![false; cubes.len()];
    let mut spots = vec![vec![(u32::MAX, 0u32); 10]; 10];

    cubes.iter().enumerate().for_each(|(cube_id, cube)| {
        let (first_id_on_height, mut max_height) =
            spots[cube.from.0 as usize][cube.from.1 as usize];

        let mut cubes_holding = Some(first_id_on_height);

        for x in cube.from.0..(cube.to.0 + 1) {
            for y in cube.from.1..(cube.to.1 + 1) {
                let (id, height) = spots[x as usize][y as usize];

                if height > max_height {
                    max_height = height;
                    cubes_holding = Some(id);
                } else if height == max_height {
                    if id != cubes_holding.unwrap_or(u32::MAX) {
                        cubes_holding = None;
                    }
                }
            }
        }

        match cubes_holding {
            Some(u32::MAX) => {}
            Some(id) => needed[id as usize] = true,
            None => {}
        }

        let new_heigh = max_height + cube.to.2 - cube.from.2 + 1;
        for x in cube.from.0..(cube.to.0 + 1) {
            for y in cube.from.1..(cube.to.1 + 1) {
                spots[x as usize][y as usize] = (cube_id as u32, new_heigh);
            }
        }
    });

    needed.iter().filter(|it| !*it).count()
}

fn main() {
    assert_eq!(part01("./input_test.txt"), 5);
    let part01_res = part01("./input_prod.txt");
    println!("Part 01: {}", part01_res);
    assert_eq!(part01_res, 463);
}
