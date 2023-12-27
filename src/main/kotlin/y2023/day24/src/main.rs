use std::fs;

use nalgebra::{Matrix3x4, Vector3};
use regex::Regex;

type Vector = Vector3<i64>;
#[derive(Debug, Clone, Copy)]
struct Hail {
    p: Vector,
    v: Vector,
}

fn read_input(file_path: &str) -> Vec<Hail> {
    let data = fs::read_to_string(file_path).unwrap();
    let re =
        Regex::new(r"([-0-9]+), ([-0-9]+), ([-0-9]+) @ ([-0-9]+), ([-0-9]+), ([-0-9]+)").unwrap();
    re.captures_iter(&data)
        .map(|c| {
            let (_, coords) = c.extract();
            let [x1, x2, x3, v1, v2, v3] = coords.map(|it| it.parse().unwrap());

            Hail {
                p: Vector::new(x1, x2, x3),
                v: Vector::new(v1, v2, v3),
            }
        })
        .collect()
}

fn colide2d(h1: &Hail, h2: &Hail) -> Option<Vector> {
    let denominator_t = h1.v.y * h2.v.x - h1.v.x * h2.v.y;

    // parallel
    if denominator_t == 0 {
        return None;
    }

    let diff = h1.p - h2.p;
    let divisor_t = diff.x * h2.v.y - diff.y * h2.v.x;
    let divisor_s = -diff.x * h1.v.y - -diff.y * h1.v.x;
    let denominator_s = -denominator_t;

    // going in different direction from each other
    if divisor_t.signum() * denominator_t.signum() < 0
        || divisor_s.signum() * denominator_s.signum() < 0
    {
        return None;
    }

    // the division precision is good enough, because we have 1 decimal space
    Some(h1.p + h1.v * (divisor_t / denominator_t))
}

fn part01(file_path: &str, region: (i64, i64)) -> usize {
    // keeps 1 decimal place
    let scale = 10;

    let boundary = (region.1 - region.0) * scale + 1;
    let offset = Vector::new(region.0, region.0, region.0);

    let hails: Vec<_> = read_input(file_path)
        .into_iter()
        .map(|hail| Hail {
            p: (hail.p - offset) * scale,
            v: hail.v,
        })
        .collect();

    let mut colisions = 0;

    for i in 0..hails.len() {
        for j in (i + 1)..hails.len() {
            let coord = match colide2d(&hails[i], &hails[j]) {
                Some(coord) => coord,
                None => {
                    continue;
                }
            };

            if (0..boundary).contains(&coord.x) && (0..boundary).contains(&coord.y) {
                colisions += 1;
            }
        }
    }

    colisions
}

fn are_paralel(h1: &Hail, h2: &Hail) -> bool {
    h1.v.cross(&h2.v) == Vector::new(0, 0, 0)
}

fn colide3d_check(h1: &Hail, h2: &Hail) -> bool {
    let denominator_y = h1.v.y * h2.v.x - h1.v.x * h2.v.y;
    let denominator_z = h1.v.z * h2.v.x - h1.v.x * h2.v.z;

    // parallel
    if denominator_y == 0 || denominator_z == 0 {
        return false;
    }

    let diff = h1.p - h2.p;
    let divisor_y = diff.x * h2.v.y - diff.y * h2.v.x;
    let divisor_z = diff.x * h2.v.z - diff.z * h2.v.x;

    divisor_y / denominator_y == divisor_z / denominator_z
}

// Finds time of intersection of hail C with the plane given by hails A and B
// uses GEM, expect to have solution, e.g. non dependent vectors have to be passed!
// does not work with unexpected zeros (it is not needed for this task)
fn find_scale(
    a_pos: Vector,
    a_vec: Vector,
    b_pos: Vector,
    b_vec: Vector,
    c_pos: Vector,
    c_vec: Vector,
) -> i64 {
    let _ = b_vec;
    let mut matrix = Matrix3x4::from_columns(&[a_vec, b_pos - a_pos, -c_vec, c_pos - a_pos]);
    // let mut matrix = Matrix3x4::from_columns(&[a_vec, b_vec, -c_vec, c_pos - a_pos]);
    let (height, width) = matrix.shape();

    for i in 0..(height - 1) {
        let base = matrix.row(i).column(i).x;
        if base == 0 {
            panic!("To complex, sorry...");
        }
        for j in (i + 1)..height {
            let local = matrix.row(j).column(i).x;
            matrix.set_row(j, &(local * matrix.row(i) + -1 * base * matrix.row(j)));
        }
    }

    fn gcd(mut n: i64, mut m: i64) -> i64 {
        while m != 0 {
            if m < n {
                std::mem::swap(&mut m, &mut n);
            }
            m %= n;
        }
        n
    }

    // redundant for this use case
    for i in 0..height {
        let i = height - i - 1;

        let base = matrix.row(i).column(i).x;
        if base == 0 {
            panic!("To complex, to lazy...");
        }

        for j in 0..i {
            let local = matrix.row(j).column(i).x;
            matrix.set_row(j, &(local * matrix.row(i) + -1 * base * matrix.row(j)));
        }

        // normalize
        let base_row = matrix.row(i);
        let gcd = base_row.iter().filter(|it| **it != 0).map(|it| it.abs()).reduce(|acu, it| gcd(acu, it)).unwrap();
        matrix.set_row(i, &(base_row / gcd * base.signum()));
    }

    let last_row = matrix.row(height - 1);
    last_row.column(width - 1).x / last_row.column(width - 2).x
}

// h1, h2 - in the same plane
// h3, h4 - outside
fn find_non_parallel(hails: &Vec<Hail>) -> ((Hail, Hail), (Hail, Hail)) {
    let mut one_plane = None;

    'parent: for i in 1..hails.len() {
        for j in (i + 1)..hails.len() {
            let h1 = &hails[i];
            let h2 = &hails[j];
            if !are_paralel(h1, h2) && !colide3d_check(h1, h2) {
                continue;
            }
            one_plane = Some((hails[i], hails[j]));
            break 'parent;
        }
    }
    let in_one_plane = if let Some(hails) = one_plane {
        hails
    } else {
        panic!("There are no parallel nor colliding hails, you need to do something else...");
    };

    let mut iter = hails
        .iter()
        .filter(|hail| !are_paralel(hail, &in_one_plane.0))
        .take(2);
    let crossing = (*iter.next().unwrap(), *iter.next().unwrap());

    (in_one_plane, crossing)
}

fn part02(file_path: &str) -> i64 {
    let hails: Vec<_> = read_input(file_path);

    // h1, h2 -> form a plane
    // h3, h4 -> intersections
    let ((h1, h2), (h3, h4)) = find_non_parallel(&hails);

    let r = find_scale(h2.p, h2.v, h1.p, h1.v, h3.p, h3.v);
    let c = h3.p + h3.v * r;
    let o = find_scale(h1.p, h1.v, h2.p, h2.v, h4.p, h4.v);
    let d = h4.p + h4.v * o;

    let x_vel = (c - d) / (r - o);
    let x_pos = c - r * x_vel;
    println!("{:?} -> {:?}", x_pos, x_vel);
    x_pos.sum()
}

fn main() {
    assert_eq!(part01("./input_test.txt", (7, 27)), 2);
    let part01_res = part01("./input_prod.txt", (200000000000000, 400000000000000));
    println!("Part 01: {}", part01_res);
    assert_eq!(part01_res, 16727);

    assert_eq!(part02("./input_test.txt"), 47);
    let part02_res = part02("./input_prod.txt");
    println!("Part 02: {}", part02_res);
    assert_eq!(part02_res, 0);
}
