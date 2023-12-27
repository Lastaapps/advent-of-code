use std::{
    collections::{HashMap, VecDeque},
    fs,
};

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

    fn is_visited(&self, point: &Point) -> bool {
        self.visited[point.0 as usize * self.size + point.1 as usize]
    }

    fn get_distance(&self, point: &Point) -> u32 {
        self.distances[point.0 as usize * self.size + point.1 as usize]
    }
}

#[inline]
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

fn seach_path_grid(
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
            seach_path_grid(map, traveler, neighbour, end, ignore_hikes, distance + 1)
        });

    traveler.unvisit(point);
}

fn solve_grid(file_path: &str, ignore_hikes: bool) -> u32 {
    let (map, start, end) = read_input(file_path);
    let mut traveler = Traveler::new(map.size);

    seach_path_grid(&map, &mut traveler, &start, &end, ignore_hikes, 0);

    traveler.get_distance(&end) + 2
}

struct Dušan {
    size: usize,
    visited: Vec<bool>,
    junctions: Vec<bool>,
}

impl Dušan {
    fn new(size: usize) -> Dušan {
        Dušan {
            size,
            visited: vec![false; size * size],
            junctions: vec![false; size * size],
        }
    }

    fn visit(&mut self, point: &Point) {
        self.visited[point.0 as usize * self.size + point.1 as usize] = true
    }

    fn is_visited(&self, point: &Point) -> bool {
        self.visited[point.0 as usize * self.size + point.1 as usize]
    }

    fn mark_junction(&mut self, point: &Point) {
        self.junctions[point.0 as usize * self.size + point.1 as usize] = true
    }

    fn is_junction(&self, point: &Point) -> bool {
        self.junctions[point.0 as usize * self.size + point.1 as usize]
    }
}

type Graph = Vec<Vec<(u32, u32)>>;

fn create_graph(map: &Map, start: &Point, end: &Point) -> (Graph, u32, u32) {
    let mut junction_id = 0u32;
    let mut junctions: HashMap<Point, u32> = HashMap::new();

    let mut stack: VecDeque<(Point, Point, u32)> = VecDeque::with_capacity(32);
    // 30% faster when using Dušan to track junctions
    let mut dusan = Dušan::new(map.size);
    let mut graph: Graph = Vec::with_capacity(32);

    fn create_junction(
        junctions: &mut HashMap<(u32, u32), u32>,
        dusan: &mut Dušan,
        junction_id: &mut u32,
        graph: &mut Vec<Vec<(u32, u32)>>,
        point: &Point,
    ) -> u32 {
        let my_id = *junction_id;
        *junction_id += 1;
        junctions.insert(*point, my_id);
        dusan.mark_junction(point);
        graph.push(Vec::new());
        my_id
    }
    create_junction(
        &mut junctions,
        &mut dusan,
        &mut junction_id,
        &mut graph,
        start,
    );
    create_junction(
        &mut junctions,
        &mut dusan,
        &mut junction_id,
        &mut graph,
        end,
    );

    fn valid_neighbours(
        map: &Map,
        dusan: &Dušan,
        my_junction: &Point,
        point: &Point,
    ) -> Vec<Point> {
        neighbours(map, &point, true)
            .into_iter()
            .filter(|neighbour| {
                let is_forest = map.get(neighbour) == Tile::Forest;
                let is_visited = dusan.is_visited(neighbour);
                let is_other_junction = dusan.is_junction(neighbour) && neighbour != my_junction;
                !is_forest && (!is_visited || is_other_junction)
            })
            .collect()
    }

    valid_neighbours(&map, &dusan, &(u32::MAX, u32::MAX), start)
        .into_iter()
        .map(|neighbour| (*start, neighbour, 1))
        .for_each(|data| stack.push_back(data));
    dusan.visit(start);

    while !stack.is_empty() {
        let (junction, point, distance) = stack.pop_back().unwrap();

        if dusan.is_junction(&point) {
            let my_id = *junctions.get(&point).unwrap();
            let dest_id = *junctions.get(&junction).unwrap();

            graph[dest_id as usize].push((my_id, distance));
            graph[my_id as usize].push((dest_id, distance));

            continue;
        }

        dusan.visit(&point);

        let neighbours: Vec<_> = valid_neighbours(&map, &dusan, &junction, &point);
        match neighbours.len() {
            0 => {}
            // path
            1 => {
                stack.push_back((junction, neighbours[0], distance + 1));
            }
            // junction
            _ => {
                let my_id = create_junction(
                    &mut junctions,
                    &mut dusan,
                    &mut junction_id,
                    &mut graph,
                    &point,
                );
                let dest_id = *junctions.get(&junction).unwrap();

                graph[dest_id as usize].push((my_id, distance));
                graph[my_id as usize].push((dest_id, distance));

                neighbours.into_iter().for_each(|neighbour| {
                    stack.push_back((point, neighbour, 1));
                })
            }
        };
    }

    (
        graph,
        *junctions.get(start).unwrap(),
        *junctions.get(end).unwrap(),
    )
}

struct Radek {
    distances: Vec<u32>,
    visited: Vec<bool>,
}

impl Radek {
    fn new(size: usize) -> Radek {
        Radek {
            distances: vec![0u32; size],
            visited: vec![false; size],
        }
    }

    fn visit(&mut self, point: u32, distance: u32) {
        self.distances[point as usize] = self.distances[point as usize].max(distance);

        self.visited[point as usize] = true
    }

    fn unvisit(&mut self, point: u32) {
        self.visited[point as usize] = false
    }

    fn is_visited(&self, point: u32) -> bool {
        self.visited[point as usize]
    }

    fn get_distance(&self, point: u32) -> u32 {
        self.distances[point as usize]
    }
}

#[allow(dead_code)]
fn search_path_graph_recursive(
    graph: &Graph,
    radek: &mut Radek,
    point: u32,
    end: u32,
    distance: u32,
) {
    radek.visit(point, distance);

    if point == end {
        radek.unvisit(point);
        return;
    }

    graph[point as usize]
        .iter()
        .for_each(|(neighbour, length)| {
            // filter had problems with borrow checker
            if !radek.is_visited(*neighbour) {
                search_path_graph_recursive(graph, radek, *neighbour, end, distance + length)
            }
        });

    radek.unvisit(point);
}

#[allow(dead_code)]
// ~3% slower than the native stack
fn search_path_graph_iterative(
    graph: &Graph,
    radek: &mut Radek,
    start: u32,
    end: u32,
) {
    let mut stack = Vec::with_capacity(1usize << 13);
    stack.push((start, 0));

    while !stack.is_empty() {
        let (point, distance) = stack.pop().unwrap();

        if distance != u32::MAX {
            // open
            radek.visit(point, distance);
            stack.push((point, u32::MAX));

            if point == end {
                continue;
            }

            graph[point as usize]
                .iter()
                .filter(|(neighbour, _)| { !radek.is_visited(*neighbour)})
                .for_each(|(neighbour, length)| {
                    stack.push((*neighbour, distance + length))
                });
        } else {
            // close
            radek.unvisit(point);
        }
    }
}

fn search_path_graph(graph: &Graph, radek: &mut Radek, start: u32, end: u32) {
    // search_path_graph_recursive(graph, radek, start, end, 0)
    search_path_graph_iterative(graph, radek, start, end)
}

fn solve_grap(file_path: &str) -> u32 {
    let (map, start, end) = read_input(file_path);
    let (graph, start, end) = create_graph(&map, &start, &end);
    let mut radek = Radek::new(graph.len());

    search_path_graph(&graph, &mut radek, start, end);

    radek.get_distance(end) + 2
}

fn main() {
    assert_eq!(solve_grid("./input_test.txt", false), 94);
    let part01_res = solve_grid("./input_prod.txt", false);
    println!("Part 01: {}", part01_res);
    assert_eq!(part01_res, 2110);

    assert_eq!(solve_grap("./input_test.txt"), 154);
    let part02_res = solve_grap("./input_prod.txt");
    println!("Part 02: {}", part02_res);
    assert_eq!(part02_res, 6514);
}
