use regex::Regex;
use std::{
    collections::VecDeque,
    fs,
};

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

fn solve(file_path: &str) -> (usize, usize) {
    let mut cubes = read_input(file_path);
    cubes.sort_by(|a, b| a.from.2.cmp(&b.from.2));

    let mut needed = vec![false; cubes.len()];
    let mut spots = vec![vec![(u32::MAX, 0u32); 10]; 10];
    let mut supports = vec![Vec::new(); cubes.len()];
    let mut supported = vec![Vec::new(); cubes.len()];

    cubes.iter().enumerate().for_each(|(cube_id, cube)| {
        let cube_id = cube_id as u32;
        let (first_id_on_height, mut max_height) =
            spots[cube.from.0 as usize][cube.from.1 as usize];

        let mut cubes_holding = vec![first_id_on_height];

        for x in cube.from.0..(cube.to.0 + 1) {
            for y in cube.from.1..(cube.to.1 + 1) {
                let (id, height) = spots[x as usize][y as usize];

                if height > max_height {
                    max_height = height;
                    cubes_holding.clear();
                    cubes_holding.push(id);
                } else if height == max_height {
                    if !cubes_holding.contains(&id) {
                        cubes_holding.push(id);
                    }
                }
            }
        }

        match cubes_holding[..] {
            [u32::MAX] => {}
            [_, ..] => {
                if cubes_holding.len() == 1 {
                    needed[cubes_holding[0] as usize] = true;
                }
                for id in cubes_holding.iter() {
                    supports[*id as usize].push(cube_id);
                }
                supported[cube_id as usize] = cubes_holding
            }
            [] => {}
        }

        let new_heigh = max_height + cube.to.2 - cube.from.2 + 1;
        for x in cube.from.0..(cube.to.0 + 1) {
            for y in cube.from.1..(cube.to.1 + 1) {
                spots[x as usize][y as usize] = (cube_id as u32, new_heigh);
            }
        }
    });

    let part01_res = needed.iter().filter(|it| !*it).count();

    // does not provide noticeable performance boost (even little slower)
    let to_break = needed
        .iter()
        .enumerate()
        .filter(|(_, it)| **it)
        .map(|(index, _)| index as u32);
    // let to_break = 0..(cubes.len());

    let part02_res = to_break
        .map(|cube_id| {
            let mut colapsed_cubes = 0;
            // not faster than Vec for this data
            let mut queue: VecDeque<u32> = VecDeque::with_capacity(32);
            // 2x faster than using a HashSet with open addressing
            let mut poped = vec![false; cubes.len()];

            queue.append(&mut supports[cube_id as usize].clone().into());
            poped[cube_id as usize] = true;

            while !queue.is_empty() {
                let next_id = queue.pop_front().unwrap();
                if poped[next_id as usize] { continue; }

                // check if all the supporting nodes are popped
                if supported[next_id as usize]
                    .iter()
                    .any(|it| !poped[*it as usize])
                {
                    continue;
                }

                // I should check if the item was already added
                // on the other hand only 6% loop iteration are caused by this
                queue.append(&mut supports[next_id as usize].clone().into());
                poped[next_id as usize] = true;
                colapsed_cubes += 1;
            }

            colapsed_cubes
        })
        .sum();

    (part01_res, part02_res)
}

fn main() {
    assert_eq!(solve("./input_test.txt"), (5, 7));
    let solution = solve("./input_prod.txt");
    println!("Part 01: {}", solution.0);
    println!("Part 02: {}", solution.1);
    assert_eq!(solution, (463, 89727));
}
