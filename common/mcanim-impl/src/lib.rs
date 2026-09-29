use std::{
    ffi::{CStr, c_char},
    fmt::Error,
    fs::write,
    io::Write,
    slice, vec,
};

use byteorder::{LittleEndian, WriteBytesExt};
use flate2::{Compression, write::ZlibEncoder};
use webp_animation::{Decoder, Frame};
struct McAnimFrame {
    timestamp: i32,
    lenght: u32,
    data: Vec<u8>,
}

impl McAnimFrame {
    fn new(timestamp: i32, lenght: u32, data: Vec<u8>) -> Self {
        Self {
            timestamp,
            lenght,
            data,
        }
    }

    pub fn to_bytes(self) -> Vec<u8> {
        let mut wtr: Vec<u8> = vec![];

        wtr.write_i32::<LittleEndian>(self.timestamp).unwrap();
        wtr.write_u32::<LittleEndian>(self.lenght).unwrap();
        wtr.write_all(&self.data).unwrap();
        wtr
    }
}

struct McAnim {
    file_name: String,
    magic_bytes: [u8; 6],
    major: u8,
    minor: u8,
    patch: u8,
    data_offset: u32,
    frames: u32,
    width: u32,
    height: u32,
    payload: Vec<McAnimFrame>,
}

impl McAnim {
    pub fn from_webp(buffer: &[u8], file_name: String) -> Result<Self, Error> {
        let decoder = Decoder::new(buffer).unwrap();
        let frames_iterator = decoder.into_iter();
        let frames: Vec<Frame> = frames_iterator.collect();
        let frame_count = frames.len() as u32;

        let mut mc_anim_frames: Vec<McAnimFrame> = vec![];

        for frame in &frames {
            let mut encoder = ZlibEncoder::new(Vec::new(), Compression::best());
            encoder.write_all(frame.data()).unwrap();

            let compression = encoder.finish().unwrap();
            let timestamp: i32 = frame.timestamp();
            let lenght: u32 = compression.len() as u32;

            mc_anim_frames.push(McAnimFrame::new(timestamp, lenght, compression));
        }

        Ok(Self {
            file_name: file_name,
            magic_bytes: "MCANIM".as_bytes().try_into().unwrap(),
            major: 1,
            minor: 0,
            patch: 0,
            data_offset: 25,
            frames: frame_count,
            width: frames[0].dimensions().0,
            height: frames[0].dimensions().1,
            payload: mc_anim_frames,
        })
    }

    pub fn to_bytes(self) -> Vec<u8> {
        let mut wtr: Vec<u8> = vec![];

        wtr.write_all(&self.magic_bytes).unwrap();
        wtr.write_u8(self.major).unwrap();
        wtr.write_u8(self.minor).unwrap();
        wtr.write_u8(self.patch).unwrap();
        wtr.write_u32::<LittleEndian>(self.data_offset).unwrap();
        wtr.write_u32::<LittleEndian>(self.frames).unwrap();
        wtr.write_u32::<LittleEndian>(self.width).unwrap();
        wtr.write_u32::<LittleEndian>(self.height).unwrap();
        for anim in self.payload {
            wtr.write_all(&anim.to_bytes()).unwrap();
        }

        wtr
    }

    pub fn write(self, mut path: String) -> String {
        path.push_str("/");
        path.push_str(&self.file_name);
        write(&path, self.to_bytes()).unwrap();

        path
    }
}

#[unsafe(no_mangle)]
pub extern "C" fn convert_webp(
    data_ptr: *const u8,
    len: usize,
    file_name_ptr: *const c_char,
    out_dir_ptr: *const c_char,
) -> bool {
    if data_ptr.is_null() || file_name_ptr.is_null() || out_dir_ptr.is_null() {
        return false;
    }

    let bytes = unsafe { slice::from_raw_parts(data_ptr, len) };

    let file_name = unsafe { CStr::from_ptr(file_name_ptr) }
        .to_string_lossy()
        .into_owned();
    let out_dir = unsafe { CStr::from_ptr(out_dir_ptr) }
        .to_string_lossy()
        .into_owned();

    if let Ok(anim) = McAnim::from_webp(bytes, file_name) {
        anim.write(out_dir);
        true
    } else {
        false
    }
}