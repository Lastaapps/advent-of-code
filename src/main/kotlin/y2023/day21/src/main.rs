use std::{fs, vec};

type Point = (usize, usize);
struct Input {
    start: Point,
    map: Map,
}

struct Map {
    size: usize,
    data: Vec<bool>,
}

impl Map {
    fn new(size: usize) -> Map {
        let data = vec![false; size * size];
        Map { size, data }
    }
    fn set(&mut self, pos: Point, val: bool) {
        self.data[pos.0 * self.size + pos.1] = val
    }
    fn get(&self, pos: Point) -> bool {
        self.data[pos.0 * self.size + pos.1]
    }
}

fn read_map(file_path: &str) -> Input {
    let data = fs::read_to_string(file_path).unwrap();
    let size = data.lines().next().unwrap().len();

    let mut map = Map::new(size);
    let mut start: Option<Point> = None;

    data.lines().enumerate().for_each(|(y, line)| {
        line.chars().enumerate().for_each(|(x, char)| match char {
            '.' => map.set((x, y), true),
            'S' => {
                start = Some((x, y));
                map.set((x, y), true);
            }
            _ => {}
        })
    });

    Input {
        start: start.unwrap(),
        map,
    }
}

struct PathTracker {
    size: usize,
    data: Vec<bool>,
}

impl PathTracker {
    fn new(size: usize) -> PathTracker {
        let data = vec![false; size * size * 2];
        PathTracker { size, data }
    }

    fn visit(&mut self, point: &Point, is_even: bool) {
        let offset = if is_even { self.size * self.size } else { 0 };
        self.data[offset + point.0 * self.size + point.1] = true;
    }

    fn is_visited(&self, point: &Point, is_even: bool) -> bool {
        let offset = if is_even { self.size * self.size } else { 0 };
        self.data[offset + point.0 * self.size + point.1]
    }
}

fn neighbours(point: &Point, size: usize) -> Vec<Point> {
    [(usize::MAX, 0), (1, 0), (0, usize::MAX), (0, 1)]
        .iter()
        .map(|offset| {
            (
                offset.0.wrapping_add(point.0),
                offset.1.wrapping_add(point.1),
            )
        })
        .filter(|point| point.0 < size && point.1 < size)
        .collect()
}

fn walk_steps(input: Input, steps: usize) -> usize {
    let size = input.map.size;
    let mut dist = 0;
    let mut even_cnt = 0;
    let mut queue_now = vec![];
    let mut queue_next = vec![];
    let mut tracker = PathTracker::new(size);

    {
        let start = input.start;
        tracker.visit(&start, true);
        queue_now.push(start);
        even_cnt += 1;
    }

    while dist <= steps {
        let is_even = dist % 2 == 0;

        queue_now.iter().for_each(|point| {
            neighbours(point, size).into_iter().for_each(|neighbour| {
                if tracker.is_visited(&neighbour, !is_even) {
                    return;
                }
                if !input.map.get(neighbour) {
                    return;
                }

                tracker.visit(&neighbour, !is_even);
                queue_next.push(neighbour);

                if !is_even {
                    even_cnt += 1;
                }
            })
        });

        dist += 1;
        (queue_now, queue_next) = (queue_next, queue_now);
    }

    even_cnt
}

fn part01(file_path: &str, steps: usize) -> usize {
    let input = read_map(file_path);
    walk_steps(input, steps)
}

fn main() {
    assert_eq!(part01("./input_test.txt", 6), 16);
    let part01_res = part01("./input_prod.txt", 64);
    println!("Part 01: {}", part01_res);
    assert_eq!(part01_res, 3814);
}
