use std::fs;

#[derive(Copy, Clone, PartialEq, Eq)]
enum Tile {
    Ground,
    Forest,
    Left,
    Right,
    Up,
    Down,
}

type Point = (u32, u32);
struct Map {
    size: usize,
    data: Vec<Tile>,
}

impl Map {
    fn new(size: usize) -> Map {
        Map {
            size,
            data: vec![Tile::Ground; size * size],
        }
    }

    fn set(&mut self, point: &Point, tile: Tile) {
        self.data[point.0 as usize * self.size + point.1 as usize] = tile
    }

    fn get(&self, point: &Point) -> Tile {
        self.data[point.0 as usize * self.size + point.1 as usize]
    }
}

fn read_input(file_path: &str) -> (Map, Point, Point) {
    let data = fs::read_to_string(file_path).unwrap();
    let size = data.lines().next().unwrap().len();
    let mut map = Map::new(size);

    data.lines().enumerate().for_each(|(y, line)| {
        line.chars().enumerate().for_each(|(x, char)| {
            let tile = match char {
                '.' => Tile::Ground,
                '#' => Tile::Forest,
                '>' => Tile::Right,
                '<' => Tile::Left,
                '^' => Tile::Up,
                'v' => Tile::Down,
                _ => panic!(),
            };
            map.set(&(x as u32, y as u32), tile)
        });
    });

    let mut start = None;
    for i in 0..size {
        if map.get(&(i as u32, 0)) != Tile::Ground {
            continue;
        }
        map.set(&(i as u32, 0 as u32), Tile::Forest);
        start = Some((i as u32, 1 as u32));
        break;
    }
    let mut end = None;
    for i in 0..size {
        if map.get(&(i as u32, (size - 1) as u32)) != Tile::Ground {
            continue;
        }

        map.set(&(i as u32, (size - 1) as u32), Tile::Forest);
        end = Some((i as u32, (size - 2) as u32));
        break;
    }

    (map, start.unwrap(), end.unwrap())
}

struct Traveler {
    size: usize,
    distances: Vec<u32>,
    visited: Vec<bool>,
}

impl Traveler {
    fn new(size: usize) -> Traveler {
        Traveler {
            size,
            distances: vec![0u32; size * size],
            visited: vec![false; size * size],
        }
    }

    fn visit(&mut self, point: &Point, distance: u32) {
        self.distances[point.0 as usize * self.size + point.1 as usize] =
            self.distances[point.0 as usize * self.size + point.1 as usize].max(distance);

        self.visited[point.0 as usize * self.size + point.1 as usize] = true
    }

    fn unvisit(&mut self, point: &Point) {
        self.visited[point.0 as usize * self.size + point.1 as usize] = false
    }

    fn get_distance(&self, point: &Point) -> u32 {
        self.distances[point.0 as usize * self.size + point.1 as usize]
    }

    fn is_visited(&self, point: &Point) -> bool {
        self.visited[point.0 as usize * self.size + point.1 as usize]
    }
}

fn neighbours(map: &Map, point: &Point, ignore_hikes: bool) -> Vec<(u32, u32)> {
    let iter = if ignore_hikes {
        [(1, 0), (-1, 0), (0, 1), (0, -1)].iter()
    } else {
        match map.get(point) {
            Tile::Ground => [(1, 0), (-1, 0), (0, 1), (0, -1)].iter(),
            Tile::Forest => [].iter(),
            Tile::Left => [(-1, 0)].iter(),
            Tile::Right => [(1, 0)].iter(),
            Tile::Up => [(0, -1)].iter(),
            Tile::Down => [(0i32, 1i32)].iter(),
        }
    };
    iter.map(|offset| {
        (
            point.0.wrapping_add(offset.0 as u32),
            point.1.wrapping_add(offset.1 as u32),
        )
    })
    .collect()
}

fn seach_path(
    map: &Map,
    traveler: &mut Traveler,
    point: &Point,
    end: &Point,
    ignore_hikes: bool,
    distance: u32,
) {
    if map.get(point) == Tile::Forest || traveler.is_visited(point) {
        return;
    }

    traveler.visit(point, distance);

    if point == end {
        traveler.unvisit(point);
        return;
    }

    neighbours(map, point, ignore_hikes)
        .iter()
        .for_each(|neighbour| {
            seach_path(map, traveler, neighbour, end, ignore_hikes, distance + 1)
        });

    traveler.unvisit(point);
}

fn solve_grid(file_path: &str, ignore_hikes: bool) -> u32 {
    let (map, start, end) = read_input(file_path);
    let mut traveler = Traveler::new(map.size);

    seach_path(&map, &mut traveler, &start, &end, ignore_hikes, 0);

    traveler.get_distance(&end) + 2
}

fn main() {
    assert_eq!(solve_grid("./input_test.txt", false), 94);
    let part01_res = solve_grid("./input_prod.txt", false);
    println!("Part 01: {}", part01_res);
    assert_eq!(part01_res, 2110);

    assert_eq!(solve_grid("./input_test.txt", true), 154);
    let part02_res = solve_grid("./input_prod.txt", true);
    println!("Part 02: {}", part02_res);
    assert_eq!(part02_res, 6514);
}
